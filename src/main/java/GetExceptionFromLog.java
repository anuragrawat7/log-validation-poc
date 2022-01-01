import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class GetExceptionFromLog {

    static String fileSeparator = "=====================================================================================================================================================================";
    static String exceptionSeparator = "----------------------------------------------------------------------------------------------------------------------------------------------------------------";
    static Map<String, Integer> uniqueException = new HashMap<>();

    static XSSFWorkbook workbook = new XSSFWorkbook();
    static XSSFSheet DASHBOARD_SHEET = workbook.createSheet("Dashboard");
    static XSSFSheet EXCEPTION_SHEET = workbook.createSheet("Exception Log");

    /*DOC: Get all the exceptions from the log files in the specified folder*/
    public static String getException(String folderPath){
        if(folderPath==null || folderPath.equals("")) {
            return "FAIL";
        }
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        SimpleDateFormat sdf3 = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String fileName = folderPath + File.separator + "log" + sdf3.format(timestamp) + ".xlsx";

        String longText = "";
        String temporaryText = "";
        String path;
        DecimalFormat df = new DecimalFormat("###.##");
        String result = "FAIL";
        try {
            longText += "Total Number of Files Read: " + listOfFiles.length + "\n" + fileSeparator +"\n";

            Map<Integer, Object[]> DATA = new TreeMap<>();
            DATA.put(1, new Object[] { "Total Number of Files Read", String.valueOf(listOfFiles.length)});
            XSSFRow row;
            Set<Integer> keyid = DATA.keySet();
            int rowid = 0;
            for (Integer key : keyid) {
                row = DASHBOARD_SHEET.createRow(rowid++);
                Object[] objectArr = DATA.get(key);
                int cellid = 0;
                for (Object obj : objectArr) {
                    Cell cell = row.createCell(cellid++);
                    cell.setCellValue((String) obj);
                }

                FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                workbook.write(fileOutputStream);
                fileOutputStream.close();
            }

            float len = listOfFiles.length;
            for (int i = 0; i < len; i++) {
                if (listOfFiles[i].isFile()) {
                    System.err.println(i+1 + "- Size of file "+listOfFiles[i].length()/1024+" kb");
                    path = folderPath + File.separator + listOfFiles[i].getName();
                    String out = readLogFile(path, fileName);
                    if(out != null) {
                        temporaryText += out + fileSeparator+ "\n";
                    }
                }

                float num = i+1;
                float cur= (num/len) *100;
                System.out.println(df.format(cur) +"%");
            }

            FileInputStream inputStream = new FileInputStream(fileName);
            XSSFWorkbook WORKBOOK = (XSSFWorkbook) WorkbookFactory.create(inputStream);
            XSSFSheet spreadsheet = WORKBOOK.getSheetAt(0);

            longText += "Unique Exceptions: " + uniqueException.size() + "\n\n";
            for(Map.Entry<String, Integer> exception : uniqueException.entrySet()){
                longText += exception.getKey() + " : " + exception.getValue() + "\n";
            }

            //After readLogFile() method
            DATA.put(2, new Object[] { "Unique exception ", String.valueOf(uniqueException.size())});

            DATA.put(3, new Object[] { " "});
            DATA.put(4, new Object[] { "Exception Name "," Number of times occurred "});
            for (int i = 5;i<=uniqueException.size()+4;i++){
                for (Map.Entry<String, Integer> exception : uniqueException.entrySet()) {
                    DATA.put(i, new Object[]{ exception.getKey(), String.valueOf(exception.getValue())});
                    i++;
                }
            }
            int rowId2 = spreadsheet.getLastRowNum();
            for (Integer key : keyid) {
                row = spreadsheet.createRow(rowId2++);

                Object[] objectArr = DATA.get(key);
                int cellid = 0;

                for (Object obj : objectArr) {
                    Cell cell = row.createCell(cellid++);
                    cell.setCellValue((String)obj);

                }
            }
            inputStream.close();

            FileOutputStream out = new FileOutputStream(fileName);
            WORKBOOK.write(out);
            out.close();

            longText += fileSeparator + "\n";
            longText += temporaryText;
            result = givenWritingStringToFile_whenUsingFileOutputStream_thenCorrect(longText, folderPath);
        }catch(Exception e) {
            System.out.println(e);
            return result;
        }

        return result;
    }

    // Read each file and generate text
    public static String readLogFile(String path, String fileName) throws ParseException, IOException {
        String text = null;
        String[] arrValues = path.split(Pattern.quote(File.separator));
        @SuppressWarnings("resource")
        BufferedReader reader = new BufferedReader(
                new FileReader(path));
        StringBuilder toFile = new StringBuilder();;
        String line, nextLine;
        toFile.append("File Name: "+arrValues[arrValues.length - 1]);
        toFile.append(System.lineSeparator());
        Map<Integer, String> map = new HashMap<Integer, String>();
        int currentLine = 0, previousPosition;

        StringBuilder PreviousLines = new StringBuilder();
        StringBuilder AfterLines = new StringBuilder();
        int excel_Rows = 2;
        FileInputStream inputStream = new FileInputStream(fileName);
        XSSFWorkbook WORKBOOK = (XSSFWorkbook) WorkbookFactory.create(inputStream);
        XSSFSheet spreadsheet = WORKBOOK.getSheetAt(1);
        Map<Integer, Object[]> LOG_DATA = new TreeMap<>();
        LOG_DATA.put(1, new Object[] {"File Name " , "Line number", "Previous Line ", "After Line " });

        while ((line = reader.readLine()) != null) {
            reader.mark(5000);
            reader.markSupported();
            currentLine++;
            map.put(currentLine, line);
            if (line.contains("Exception")) {

                // Count unique exceptions
                if(line.contains("INFO:")) {
                    line = line.substring(line.indexOf("INFO") + 6);
                }

                if(uniqueException.containsKey(line)){
                    uniqueException.put(line, uniqueException.get(line)+1);
                }
                else {
                    uniqueException.put(line, 1);
                }
                // To print the line where exception occurred
                toFile.append("Line Number: " + currentLine);
                toFile.append(System.lineSeparator());

                // To read previous 25 lines from the current exception
                previousPosition = currentLine - 2;
                for (int i = 0; i <=2; i++) {
                    if (map.get(previousPosition) != null) {
                        toFile.append(map.get(previousPosition));
                        toFile.append(System.lineSeparator());

                        PreviousLines.append(map.get(previousPosition));

                    }
                    previousPosition++;
                }

                // To read next 50 lines from the current exception
                for (int i = currentLine; i < (currentLine + 2); i++) {
                    nextLine = reader.readLine();
                    if (nextLine != null) {
                        toFile.append(nextLine);
                        toFile.append(System.lineSeparator());

                        AfterLines.append(nextLine);

                    } else {
                        break;
                    }

                }

                toFile.append(exceptionSeparator);
                toFile.append(System.lineSeparator());
                text = toFile.toString();

                LOG_DATA.put(excel_Rows, new Object[]{ arrValues[arrValues.length - 1], String.valueOf(currentLine), PreviousLines.toString(), AfterLines.toString()});
                excel_Rows++;

            }

            reader.reset();
        }

        Set<Integer> keyid = LOG_DATA.keySet();
        int rowid=0;

        for (Integer key : keyid) {

            XSSFRow row = spreadsheet.createRow(rowid++);

            Object[] objectArr = LOG_DATA.get(key);
            int cellid = 0;

            for (Object obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                cell.setCellValue((String)obj);

            }
        }
        inputStream.close();

        FileOutputStream out = new FileOutputStream(fileName);
        WORKBOOK.write(out);
        out.close();

        return text;
    }


    public static String givenWritingStringToFile_whenUsingFileOutputStream_thenCorrect(String exceptionText, String folderPath) throws IOException {
        // Defining file name of the generated log  file.
        SimpleDateFormat sdf3 = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String fileName = folderPath + File.separator + "log" + sdf3.format(timestamp) + ".log";
        FileOutputStream outputStream = new FileOutputStream(fileName);
        byte[] strToBytes = exceptionText.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
        System.out.println("File is generated! Please to this location "+fileName);
        return fileName;
    }

}



































/*
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class GetExceptionFromLog {

    static String fileSeparator = "=====================================================================================================================================================================";
    static String exceptionSeparator = "----------------------------------------------------------------------------------------------------------------------------------------------------------------";
    static Map<String, Integer> uniqueException = new HashMap<>();

    static XSSFWorkbook workbook = new XSSFWorkbook();
    static XSSFSheet DASHBOARD_SHEET = workbook.createSheet("Dashboard");
    static XSSFSheet EXCEPTION_SHEET = workbook.createSheet("Exception Log");

    */
/*DOC: Get all the exceptions from the log files in the specified folder*//*

    public static String getException(String folderPath){
        if(folderPath==null || folderPath.equals("")) {
            return "FAIL";
        }
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        String longText = "";
        String temporaryText = "";
        String path;
        DecimalFormat df = new DecimalFormat("###.##");
        String result = "FAIL";
        try {
            longText += "Total Number of Files Read: " + listOfFiles.length + "\n" + fileSeparator +"\n";

            Map<Integer, Object[]> DATA = new TreeMap<>();
            DATA.put(1, new Object[] { "Total Number of Files Read -", String.valueOf(listOfFiles.length)});

            int len = listOfFiles.length;

            for (int i = 0; i < len; i++) {

                if (listOfFiles[i].isFile()) {

                    System.err.println(i+1 + "Size of file "+listOfFiles[i].length()/1024+" kb");

                    path = folderPath + File.separator + listOfFiles[i].getName();
                    String out = readLogFile(path,folderPath);
                    if(out != null) {
                        temporaryText += out + fileSeparator+ "\n";
                    }
                }

                float num = i+1;
                float cur= (num/len) *100;
                System.out.println(df.format(cur) +"%");

            }

            longText += "Unique Exceptions: " + uniqueException.size() + "\n\n";

            DATA.put(2, new Object[] { "Unique exception - ", String.valueOf(uniqueException.size())});

            for(Map.Entry<String, Integer> exception : uniqueException.entrySet()){
                longText += exception.getKey() + " : " + exception.getValue() + "\n";
            }
            DATA.put(3, new Object[] { " "});
            DATA.put(4, new Object[] { "Exception Name "," Number of times occurred "});
            for (int i = 5;i<=uniqueException.size()+4;i++){
                for (Map.Entry<String, Integer> exception : uniqueException.entrySet()) {
                    DATA.put(i, new Object[]{ exception.getKey(), String.valueOf(exception.getValue())});
                    i++;
                }
            }


            longText += fileSeparator + "\n";
            longText += temporaryText;

            XSSFRow row;
            Set<Integer> keyid = DATA.keySet();
            int rowid = 0;
            for (Integer key : keyid) {

                row = DASHBOARD_SHEET.createRow(rowid++);
                Object[] objectArr = DATA.get(key);
                int cellid = 0;

                for (Object obj : objectArr) {
                    Cell cell = row.createCell(cellid++);
                    cell.setCellValue((String) obj);
                }
            }
            SimpleDateFormat sdf3 = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String fileName = folderPath + File.separator + "log" + sdf3.format(timestamp) + ".xlsx";
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            workbook.write(fileOutputStream);
            fileOutputStream.close();

            result = givenWritingStringToFile_whenUsingFileOutputStream_thenCorrect(longText, folderPath);
        }catch(Exception e) {
            System.out.println(e);
            return result;
        }
        */
/*
        for(Map.Entry<String, Integer> mp: uniqueException.entrySet())
            System.out.println(mp.getKey()+"->"+mp.getValue());
         *//*

        return result;
    }
    // Read each file and generate text
    public static String readLogFile(String path, String folderPath) throws IOException {
        String text = null;
        String[] arrValues = path.split(Pattern.quote(File.separator));
        @SuppressWarnings("resource")
        BufferedReader reader = new BufferedReader(
                new FileReader(path));
        StringBuilder toFile = new StringBuilder();

        String line, nextLine;
        toFile.append("File Name: "+arrValues[arrValues.length - 1]);

        Map<Integer, Object[]> DATA = new TreeMap<>();

        //DATA.put(1, new Object[] {"File Name :" , arrValues[arrValues.length - 1] });
        DATA.put(1, new Object[] {"File Name " , "Line number", "Previous Line ", "After Line " });

        toFile.append(System.lineSeparator());
        Map<Integer, String> map = new HashMap<Integer, String>();
        int currentLine = 0, previousPosition;
        while ((line = reader.readLine()) != null) {
            reader.mark(5000);
            reader.markSupported();
            currentLine++;
            map.put(currentLine, line);
            if (line.contains("Exception")) {

                // Count unique exceptions
                if(line.contains("INFO:")) {
                    line = line.substring(line.indexOf("INFO") + 6);
                }

                if(uniqueException.containsKey(line)){
                    uniqueException.put(line, uniqueException.get(line)+1);
                }
                else {
                    uniqueException.put(line, 1);
                }

                // To print the line where exception occurred
                toFile.append("Line Number: " + currentLine);
                toFile.append(System.lineSeparator());

                // To read previous 25 lines from the current exception
                previousPosition = currentLine - 25;
                for (int i = 0; i <=25; i++) {
                    if (map.get(previousPosition) != null) {
                        toFile.append(map.get(previousPosition));

                        PreviousLines.append(map.get(previousPosition));

                        toFile.append(System.lineSeparator());
                    }
                    previousPosition++;
                }

                // To read next 50 lines from the current exception
                for (int i = currentLine; i < (currentLine + 50); i++) {
                    nextLine = reader.readLine();
                    if (nextLine != null) {
                        toFile.append(nextLine);

                        AfterLines.append(nextLine);

                        toFile.append(System.lineSeparator());

                    } else {
                        break;
                    }

                }

                toFile.append(exceptionSeparator);
                toFile.append(System.lineSeparator());
                text = toFile.toString();

                DATA.put(excel_Rows, new Object[]{ arrValues[arrValues.length - 1], String.valueOf(currentLine), PreviousLines.toString(), AfterLines.toString()});

                XSSFRow row;
                Set<Integer> keyid = DATA.keySet();
                int rowid = 0;
                for (Integer key : keyid) {

                    row = EXCEPTION_SHEET.createRow(rowid++);
                    Object[] objectArr = DATA.get(key);
                    int cellid = 0;

                    for (Object obj : objectArr) {
                        Cell cell = row.createCell(cellid++);
                        cell.setCellValue((String) obj);
                    }
                }
                SimpleDateFormat sdf3 = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String fileName = folderPath + File.separator + "log" + sdf3.format(timestamp) + ".xlsx";
                FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                workbook.write(fileOutputStream);
                fileOutputStream.close();

            }
            reader.reset();
        }
        return text;
    }


    public static String givenWritingStringToFile_whenUsingFileOutputStream_thenCorrect(String exceptionText, String folderPath) throws IOException {
        // Defining file name of the generated log  file.
        SimpleDateFormat sdf3 = new SimpleDateFormat("_yyyy_MM_dd_HH_mm_ss");
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String fileName = folderPath + File.separator + "log" + sdf3.format(timestamp) + ".log";
        FileOutputStream outputStream = new FileOutputStream(fileName);
        byte[] strToBytes = exceptionText.getBytes();
        outputStream.write(strToBytes);
        outputStream.close();
        System.out.println("File is generated! Please to this location "+fileName);
        return fileName;
    }
}
*/
