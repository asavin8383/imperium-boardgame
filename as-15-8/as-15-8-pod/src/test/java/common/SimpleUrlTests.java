package common;

import lombok.extern.slf4j.Slf4j;
import utils.URLComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class SimpleUrlTests
{
    public static void main(String[] args){

        //testIsEquals();
        //testValidUrls();
        //testShitUrls();
        //test7();
        //test8();
        //test9();
        //test10();
        
    }

    public static void testIsEquals(){
        List<String> list = getTestUrlsBigList();
        List<String> errors = new ArrayList<>();

        System.out.println("count = " + list.size());

        for(String str : list){
            try {
                if (!URLComponent.testUrl(str)){
                    errors.add(str);
                }
                else {
                    URLComponent com = URLComponent.fromString(str);
                    if (!str.equals(com.toString())){
                        System.out.println(str);
                    }
                }
            }
            catch (Exception e) {
                errors.add(str);
            }
        }

        System.out.println("----- Errors -----");
        for (String str : errors){
            System.out.println(str);
        }
    }

    public static void testValidUrls(){
        List<String> list = getTestFullUrls();
        List<String> errors = new ArrayList<>();

        System.out.println("===> count = " + list.size());

        for(String str : list){
            try {
                URLComponent comp = URLComponent.fromString(str);
                System.out.println("-------------");
                System.out.println(str);
                System.out.println(comp);
                System.out.println(URLComponent.getDecodedFrom(comp));
                System.out.println(URLComponent.getEncodedFrom(comp));
                System.out.println(comp.getHost());
            }
            catch (Exception e) {
                errors.add(str);
            }
        }

        System.out.println("----- Errors -----");
        for (String str : errors){
            System.out.println(str);
        }
    }


    public static void testShitUrls(){
        //List<String> list = getTestUrls();
        List<String> list = getTestUrlsBigList();
        //List<String> list = getTestFullUrls();
        List<String> shitUrlsHost = new ArrayList<>();
        List<String> shitUrlsFrag = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for(String str : list){
            try {
                URLComponent comp = URLComponent.fromString(str);

                boolean simpleHost = comp.isSimpleHost();
                boolean simpleFragment = comp.isSimpleFragment();
                boolean decodedHost = URLComponent.isDecodedHost(comp.getHost());
                boolean decodedFragment = URLComponent.isDecodedFragment(comp.getFullFragment());

                if (!simpleHost && !simpleFragment){
                    if (decodedHost && !decodedFragment)
                        shitUrlsHost.add(str);
                    if (!decodedHost && decodedFragment)
                        shitUrlsFrag.add(str);
                }
            }
            catch (Exception e) {
                errors.add(str);
            }
        }

        System.out.println("----- Shit urls host -----");
        for (String str : shitUrlsHost){
            System.out.println(str);
        }
        System.out.println("----- Shit urls frag -----");
        for (String str : shitUrlsFrag){
            System.out.println(str);
        }
        System.out.println("----- Errors -----");
        for (String str : errors){
            System.out.println(str);
        }
    }

    public static void test7(){
        List<String> list = getTestUrlsBigList();

        List<String> errors = new ArrayList<>();

        int limit = -1;
        for(String url : list){
            try {
                URLComponent uComp = URLComponent.fromString(url);
                URLComponent uCompDecoded = null;
                URLComponent uCompEncoded = null;
                try{
                    uCompDecoded = URLComponent.getDecodedFrom(uComp);
                }
                catch (Exception e){}
                try{
                    uCompEncoded = URLComponent.getEncodedFrom(uComp);
                }
                catch (Exception e){}


                boolean check = (uCompDecoded == null || uCompEncoded == null);
                check = check || (!url.equals(uCompDecoded.toString()) && !url.equals(uCompEncoded.toString()));


                if (!check)
                    continue;

                System.out.println("--------------");
                System.out.println(url);
                System.out.println(uCompDecoded != null ? uCompDecoded.toString() : null);
                System.out.println(uCompEncoded != null ? uCompEncoded.toString() : null);
            }
            catch (Exception e) {
                errors.add(url);
            }

            if (limit < 0)
                continue;
            if (--limit <= 0)
                break;
        }

        System.out.println("----- Errors -----");
        for (String str : errors){
            System.out.println(str);
        }
    }

    public static void test8(){
        List<String> list = getTestFullUrls2();
        List<String> errors = new ArrayList<>();

        int limit = -1;
        for(String url : list){
            try {
                URLComponent uComp = URLComponent.fromString(url);
                URLComponent uCompDecoded = null;
                URLComponent uCompEncoded = null;
                try{
                    uCompDecoded = URLComponent.getDecodedFrom(uComp);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                try{
                    uCompEncoded = URLComponent.getEncodedFrom(uComp);
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                System.out.println("--------------");
                System.out.println(url);
                System.out.println(uCompDecoded != null ? uCompDecoded.toString() : null);
                System.out.println(uCompEncoded != null ? uCompEncoded.toString() : null);
            }
            catch (Exception e) {
                errors.add(url);
            }

            if (limit > 0 && --limit == 0)
                break;
        }

        System.out.println("----- Errors -----");
        for (String str : errors){
            System.out.println(str);
        }
    }

    public static void test9(){
        List<String> list = getTestUrlsBigList();
        List<String> errors = new ArrayList<>();

        for(String url : list){
            if (!URLComponent.testUrl(url))
                errors.add(url);
            else {
                try{
                    URLComponent comp = URLComponent.fromString(url);
                    URLComponent compDecoded = URLComponent.getDecodedFrom(comp);
                    URLComponent compEncoded = URLComponent.getEncodedFrom(comp);

                    if (!url.equals(compDecoded.toString()) && !url.equals(compEncoded.toString())){
                        System.out.println("-------------");
                        System.out.println(url);
                        System.out.println(compDecoded);
                        System.out.println(compEncoded);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        System.out.println("----- Errors -----");
        for (String str : errors){
            System.out.println(str);
        }
    }

    public static void test10(){
        List<String> list = getTestUrlsBigList();
        Map<Integer, Integer> mapCount = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        for(String url : list){
            if (!URLComponent.testUrl(url))
                errors.add(url);
            else {
                try{
                    URLComponent comp = URLComponent.fromString(url);
                    URLComponent compDecoded = URLComponent.getDecodedFrom(comp);
                    URLComponent compEncoded = URLComponent.getEncodedFrom(comp);

                    int cnt = 1;
                    if (!url.equals(compDecoded.toString()))
                        cnt++;
                    if (!url.equals(compEncoded.toString()))
                        cnt++;

                    Integer i = mapCount.get(cnt);
                    i = i == null ? 0 : i;
                    mapCount.put(cnt, ++i);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        System.out.println("----- Map count -----");
        System.out.println(mapCount);
        System.out.println("----- Errors -----");
        for (String str : errors){
            System.out.println(str);
        }
    }


    // ================================================================================
    // ================================================================================

    public static List<String> getTestUrls(){
        String [] strs = {
                "123",
                "Я%20за%203ОЖ,%20всех%20на%20нож,%20кто%20на%20людей%20не%20похож...",
                "%D0%AF%20%D0%B7%D0%B0%203%D0%9E%D0%96,%20%D0%B2%D1%81%D0%B5%D1%85%20%D0%BD%D0%B0%20%D0%BD%D0%BE%D0%B6,%20%D0%BA%D1%82%D0%BE%20%D0%BD%D0%B0%20%D0%BB%D1%8E%D0%B4%D0%B5%D0%B9%20%D0%BD%D0%B5%20%D0%BF%D0%BE%D1%85%D0%BE%D0%B6...",
                "1053-%CF%E5%F0%E2%FB%E9-%E8-%EF%EE%F1%EB%E5%E4%ED%E8%E9-%F0%E0%E7...%C1%FB%EB%EE-%F1%F2%F0%E0%F8%ED%EE...",
                "%D0%B2%D1%8B%D1%80%D0%B0%D1%89%D0%B8%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5+%D0%BA%D0%BE%D0%BD%D0%BE%D0%BF%D0%BB%D0%B8",
                "/aaa?p1=123&p2=456&p3=классно",
                "/aaa?p1=123&p2=456&p3=cool",
                "/v=%D1%81%D1%83%D0%BF%D0%B5%D1%80",
                "/v=%D1%81%D1%83%D0%BF%D0%B5%D1%80",
                "/?v=+-,.%20",
                "%DO%B5%D0%B7%D0%BD%D1%8B%D0%B5"
                ,"%DO%B5%D0%B7%D0%BD%D1%8B%D0%B5-%D1%81%D1%82%D0%B0%D1%82%D1%8C%D0%B8/%D0%B4%D0%B8%D0%BF%D0%BB%D0%BE%D0%BC-%D0%B6%D0%B5%D0%BB%D0%B5%D0%B7%D0%BD%D0%BE%D0%B4%D0%BE%D1%80%D0%BE%D0%B6%D0%BD%D0%BE%D0%B3%D0%BE-%D1%82%D0%B5%D1%85%D0%BD%D0%B8%D0%BA%D1%83%D0%BC%D0%B0"
        };

        return new ArrayList<>(Arrays.asList(strs));
    }

    public static List<String> getTestFullUrls(){
        String [] strs = {
                "http://www.suicide-forum.com/showthread.php?1053-%CF%E5%F0%E2%FB%E9-%E8-%EF%EE%F1%EB%E5%E4%ED%E8%E9-%F0%E0%E7...%C1%FB%EB%EE-%F1%F2%F0%E0%F8%ED%EE...",
                "http://growerland.net/tags/%D0%B2%D1%8B%D1%80%D0%B0%D1%89%D0%B8%D0%B2%D0%B0%D0%BD%D0%B8%D0%B5+%D0%BA%D0%BE%D0%BD%D0%BE%D0%BF%D0%BB%D0%B8/",
                "веселые-коровы.весело.com/aaa?p1=123&p2=456&p3=классно",
                "веселые-коровы.com",
                "веселые-коровы_2.com/",
                "веселые-коровы_3.com/?f=123",
                "веселые-коровы_4.com/aaa/",
                "xn----ctbbkaa0bf4aavf6kh.xn--b1agaypp.com/?v=%D1%81%D1%83%D0%BF%D0%B5%D1%80",
                "*.abc.com/?v=%D1%81%D1%83%D0%BF%D0%B5%D1%80",
                "*.com/?v=*",
        };
        return new ArrayList<>(Arrays.asList(strs));
    }

    public static List<String> getTestFullUrls2(){
        String [] strs = {
                "https://librusec.pro/b/193695/download/?next=%2Fb%2F193695%3F",
                "http://kupit-pasport.ru/index.php?threads/%D0%BF%D0%BE%D0%BB%D0%B8%D1%81%D1%8B-%D0%9E%D0%A1%D0%90%D0%93%D0%9E-%D%BE%D0%BF%D1%82%D0%BE%D0%BC.131/"
        };
        return new ArrayList<>(Arrays.asList(strs));
    }

    public static List<String> getTestUrlsBigList() {
        String file = "C:\\testtest.txt";
        final List<String> list = new ArrayList<>();

        try (Stream<String> stream = Files.lines(Paths.get(file))) {
            stream.forEach(list::add);
        }
        catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return list;
    }

}
