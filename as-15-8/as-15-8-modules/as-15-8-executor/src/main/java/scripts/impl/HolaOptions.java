package scripts.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.iq80.leveldb.*;
import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import java.io.*;

/**
 * User: asinjavin
 * Date: 11.06.2019
 * Time: 20:21
 *
 * Класс работает с базой LevelDB, в которой Хром хранит настройки расширений.
 * Позволяет добавить и удалить правило для доменного имени.
 * После использования требуется закрыть базу, т.к. Lock на базе не дает запуститься Хрому.
 *
 */
public class HolaOptions implements AutoCloseable
{
    private static final String HOLA_TRUE_NAME = "gkojfkhlekighikafcpjkiklfbnlmeio";

    private static final String KEY_UNBLOCKER_RULES = "unblocker_rules";
    private static final String KEY_USED_VPN = "used_vpn";
    private static final String KEY_UUID = "uuid";
    private static final String VAL_UUID = "c69b8646b1942962afa8444d55889943";

    private ObjectMapper objectMapper = new ObjectMapper();

    private final DB db;

    HolaOptions(File chromeProfilePath) throws IOException {

        Options options = new Options();
        File holaSettings = new File(chromeProfilePath, "Default\\Local Extension Settings\\" + HOLA_TRUE_NAME);
        db = factory.open(holaSettings, options);

        checkDb();
    }

    /**
     * Инициализируем базу на случай пустого профиля
     */
    private void checkDb() {
        byte[] value = db.get(bytes(KEY_UUID));
        if (value == null)
            db.put(bytes(KEY_UUID), bytes(VAL_UUID));
    }

    /**
     * Добавление правила
     * @param name доменное имя, без протокола, например, myip.ru
     * @param country страна, откуда брать IP
     * @throws IOException
     */
    public void addRule(String name, String country) throws IOException {
        String unblocker_rules = asString(db.get(bytes(KEY_UNBLOCKER_RULES)));

        ObjectNode rules = unblocker_rules == null
                ? objectMapper.createObjectNode()
                : (ObjectNode) objectMapper.readTree(unblocker_rules);

        String key = makeKey(name, country);
        JsonNode settings = createUnblockerRule(name, country);

        rules.set(key, settings);

        db.put(bytes(KEY_UNBLOCKER_RULES), bytes(rules.toString()));
        db.put(bytes(KEY_USED_VPN), bytes("true"));
    }

    /**
     * Удаление правила
     * @param name доменное имя, без протокола, например, myip.ru
     * @param country страна, откуда брать IP
     * @throws IOException
     */
    public void delRule(String name, String country) throws IOException {
        String unblocker_rules = asString(db.get(bytes(KEY_UNBLOCKER_RULES)));
        if (unblocker_rules != null) {
            ObjectNode rules = (ObjectNode) objectMapper.readTree(unblocker_rules);
            String key = makeKey(name, country);
            rules.remove(key);
            db.put(bytes(KEY_UNBLOCKER_RULES), bytes(rules.toString()));
        }
    }


    /**
     * Создание правила
     * Структура взята из профиля хрома, AS IS
     * @param name доменное имя, без протокола, например, myip.ru
     * @param country страна, откуда брать IP
     * @return
     */
    private JsonNode createUnblockerRule(String name, String country) {
        ObjectNode settings = objectMapper.createObjectNode();
        settings.put("country", country);
        settings.put("enabled", true);
        settings.put("force_pool", false);
        settings.put("mode", "unblock");
        settings.put("name", name);
        settings.put("premium", false);
        settings.set("ts", objectMapper.createObjectNode());
        return settings;
    }

    /**
     * Название правила hola
     * @param name
     * @param country
     * @return
     */
    private String makeKey(String name, String country) {
        return String.format("%s_%s_vpn", name, country);
    }

    @Override
    public void close() throws Exception {
        if (db!= null)
            db.close();
    }

    /**
     * Отладочный дамп базы
     * @throws IOException
     */
    void dump() throws IOException {
        try (DBIterator iterator = db.iterator()) {
            for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                String key = asString(iterator.peekNext().getKey());
                String value = asString(iterator.peekNext().getValue());
                System.out.println(key + " = " + value);
            }
        }
    }


    public static void main(String[] args) throws Exception {

        HolaOptions holaOptions = new HolaOptions(new File("C:\\Users\\asinjavin\\ws\\as-15-8\\data"));

        holaOptions.dump();
//        holaOptions.addRule("myip.ru", "us");
//        holaOptions.addRule("myip.com", "us");

//        holaOptions.dump();
//        holaOptions.delRule("myip.com", "us");

        holaOptions.close();
    }

}
