package birt;

import lombok.extern.apachecommons.CommonsLog;
import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: asin
 * Date: 01.03.15
 * Time: 5:28
 */

@CommonsLog
@Service
public class BirtFacade {

    @Autowired
    DataSource dataSource;
    // различные переменные
    private IReportEngine engine;
//    private Level level = Level.WARNING;

    private final ReentrantLock lock = new ReentrantLock();


    BirtFacade() {
    }

    @PostConstruct
    void initBirtPlatform() {
        try {
            EngineConfig config = new EngineConfig();
            Platform.startup(config);
            IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
            engine = factory.createReportEngine(config);
//            engine.changeLogLevel(level);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    static void closeBirtPlatform() {
        // освобождение ресурсов
        Platform.shutdown();
    }


    void destroy() {
        // освобождение ресурсов
        engine.destroy();
        //Platform.shutdown();
    }


    public byte[] createReport(InputStream reportfile, Map<String, ?> params, String repType) throws EngineException, SQLException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        reportType(reportfile, params, ostream, repType);
        return ostream.toByteArray();
    }

    private void reportType(InputStream reportfile,
                            Map<String, ?> params,
                            OutputStream ostream,
                            String repType) throws EngineException, SQLException {

        //открываем отчет
        IReportRunnable report = engine.openReportDesign(reportfile);

        // создаем задачу
        IRunAndRenderTask task;

        lock.lock();
        try {
            task = engine.createRunAndRenderTask(report);
        } finally {
            lock.unlock();
        }

        setParams(params, report, task);

        //noinspection unchecked
        task.getAppContext().put("OdaJDBCDriverPassInConnection", dataSource.getConnection());


        IRenderOption options = getOptions(repType);

        options.setOutputStream(ostream);
        task.setRenderOption(options);

        //запуск отчета
        task.run();

        task.close();

    }

    private void setParams(Map<String, ?> params, IReportRunnable report, IRunAndRenderTask task) {
        IGetParameterDefinitionTask paramTask = engine.createGetParameterDefinitionTask(report);

        for (Map.Entry<String, ?> entry : params.entrySet()) {
            String name = entry.getKey();
            IParameterDefnBase parameterDefn = paramTask.getParameterDefn(name);
            if (parameterDefn == null) {
                log.error("Unexpected parameter " + name + " = " + entry.getValue());
                continue;
            }

            int type = parameterDefn.getParameterType();
            Object value = entry.getValue();

            switch (type) {
                case IParameterDefnBase.SCALAR_PARAMETER:
                    IScalarParameterDefn scalar = (IScalarParameterDefn) parameterDefn;
                    switch (scalar.getDataType()) {
                        case IScalarParameterDefn.TYPE_ANY:
                        case IScalarParameterDefn.TYPE_STRING:
                            task.setParameterValue(name, value.toString());
                            break;
                        case IScalarParameterDefn.TYPE_DECIMAL:
                        case IScalarParameterDefn.TYPE_FLOAT:
                            task.setParameterValue(name, Float.valueOf(value.toString()));
                            break;
                        case IScalarParameterDefn.TYPE_INTEGER:
                            task.setParameterValue(name, Integer.valueOf(value.toString()));
                            break;
                        case IScalarParameterDefn.TYPE_BOOLEAN:
                            task.setParameterValue(name, Boolean.valueOf(value.toString()));
                            break;
                        case IScalarParameterDefn.TYPE_DATE:
                            task.setParameterValue(name, parseDate(value));
                            break;
                        case IScalarParameterDefn.TYPE_DATE_TIME:
                            throw new RuntimeException("TYPE_DATE_TIME is not supported");
                        case IScalarParameterDefn.TYPE_TIME:
                            throw new RuntimeException("TYPE_TIME is not supported");
                    }
                    break;

                default:
                    throw new RuntimeException("Type '" + parameterDefn.getTypeName() + "' is not supported");

            }
        }
    }

    private Object parseDate(Object value) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return new java.sql.Date(sdf.parse((String) value).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private IRenderOption getOptions(String repType) {// настройка выходного формата отчета
        if ("html".equalsIgnoreCase(repType)) {
            HTMLRenderOption options = new HTMLRenderOption();
            options.setOutputFormat("html");
            options.setEmbeddable(false);
            return options;

        } else if ("pdf".equalsIgnoreCase(repType)) {
            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFormat("pdf");
            return options;

        } else if ("ppt".equalsIgnoreCase(repType)) {
            RenderOption options = new RenderOption();
            options.setOption(IRenderOption.EMITTER_ID, "org.eclipse.birt.report.engine.emitter.ppt");
            options.setOutputFormat("ppt");
            return options;

        } else if ("pptx".equalsIgnoreCase(repType)) {
            RenderOption options = new RenderOption();
            options.setOption(IRenderOption.EMITTER_ID, "org.eclipse.birt.report.engine.emitter.pptx");
            options.setOutputFormat("pptx");
            return options;

        } else if (repType.toLowerCase().contains("xls")) { //("xls".equalsIgnoreCase(repType)) {
            IRenderOption options = new EXCELRenderOption();
            options.setOutputFormat(repType);
            return options;

        } else if ("docx".equalsIgnoreCase(repType)) {
            IRenderOption options = new DocxRenderOption();
            options.setOutputFormat(repType);
            return options;
        }
        throw new RuntimeException("Unexpected report type '" + repType + "'");
    }

}
