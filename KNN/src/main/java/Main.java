/* *
*
* Zahid TEKBAŞ
* 151213028
* KNN Classification
* Written in Intellij IDE on Unix
*
* */

import com.opencsv.CSVReader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.lang.*;



public class Main {

    static DecimalFormat format = new DecimalFormat("0.#"); // important stuff to find classes
    static List<List<Double>> kNeighbours; // neighbours of our input
    static HashMap<String,String> listOfClassifications; // even dont remember what this thing written for. it could be useful while finding classes by float points.
    static List<Double> kNSums; // totally useless list. forgot to use it in functions. but too lazy to remove it and too nerd to write this comment section.

    public static void main(String[] args) {
        Scanner inputFile=null;
        String file=null;
        System.out.println("Please select the file");
        JFileChooser chooser = new JFileChooser(System.getProperty("java.class.path"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV files", "csv");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(null);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                inputFile= new Scanner(new File(chooser.getSelectedFile().getAbsolutePath()));
                file = chooser.getSelectedFile().getAbsolutePath();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        inputFile.close();
        kNSums = new ArrayList<>();
        listOfClassifications = new HashMap<>();
        kNeighbours = new ArrayList<>();
        List<List<String>> csv = new ArrayList<>();
        List<Double> input = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(file));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                for(int i=0; i<values.length; i++){
                    String ss = values[i];
                    char[] arr = ss.toCharArray();
                    String sb = String.valueOf(arr[0]);
                    if(sb.equals("1")){
                        values[i] = "1.0";
                    }
                    if(sb.equals("-")){
                        values[i] = "-1.0";
                    }

                }
                csv.add(Arrays.asList(values));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i=0; i<csv.size(); i++){
            System.out.println(csv.get(i)); // First row contains SL SW PL PW and Classification for Iris Dataset
        }
        List<List<String>> firstRow = new ArrayList<>();
        firstRow.add(csv.get(0));
        csv.remove(0); // Delete first row in order to calculate euclidean distance
        System.out.println("A row's size is : " + csv.get(0).size());
        int newRecordSize = csv.get(0).size()-1;
        Scanner scanner = new Scanner(System.in);
        for(int i=0; i<newRecordSize; i++){
            System.out.println("Enter your value for\t" + firstRow.get(0).get(i));
            double temp = scanner.nextDouble();
            input.add(i,temp);
        } // Storing our input to calculate its class
        System.out.println("Dostum son satirda bosluk var bu yuzden son satiri siliyorum. Bu kisimda son satiri siliyorum.");
        csv.remove(csv.size()-1);
        System.out.println("\n Enter your K parameter");
        int K;
        K=scanner.nextInt();
        scanner.close();
        // A part that handles user inputs...

        List<List<Double>> ConvertedList = new ArrayList<>();


        ConvertedList=ConvertList(csv); // String List converted to Float
        listOfClassifications=StoreClassificationValuesByValue(csv,ConvertedList); // After the calculating distance i gotta find which value refers what class
        // so i need to put them into a hashmap

        AddNewRecord(ConvertedList,K,input);
        System.out.println("Close ones by distance: ");
        for(int i=0; i<kNeighbours.size(); i++){
            System.out.println(kNeighbours.get(i));
        }

        // Parsing List<List<String>> to String[] in order to make calculations and expressions easy
        String[] firstRowString = new String[firstRow.get(0).size()-1];
        for(int i=0; i<firstRow.get(0).size()-1; i++){
            firstRowString[i] = firstRow.get(0).get(i);
        }

        getClass(kNeighbours,listOfClassifications,firstRowString,input);

        System.out.println(listOfClassifications.get(ConvertedList.get(0)));

    }


    static List<List<Double>> ConvertList(List<List<String>> list){
        int boundary = list.get(0).size()-1;
        List<List<Double>> DoubleList = new ArrayList<>();
        for(int i = 0; i<list.size(); i++){
            List<Double> tempDouble = new ArrayList<>();
            for(int j=0; j<boundary; j++){
                if(!list.get(i).get(j).isEmpty() && list.get(i).get(j) != null && list.get(i).get(j) != " "){
                    if(!list.get(i).get(j).contains(".")){
                        try{
                        double d = Double.valueOf(Integer.valueOf(list.get(i).get(j)));
                        tempDouble.add(d);
                        }
                        catch(Exception ex){
                            System.out.println("hata : "+ ex + ex.getMessage());
                        }
                    }
                    else{
                        double d = Double.valueOf(list.get(i).get(j));
                        if(d==1.0){
                            int value = 1;
                        }
                        tempDouble.add((d));
                    }
                }
                if(j == boundary-1){
                    DoubleList.add(i,tempDouble);
                }
            }
        }

        return DoubleList;
    }

    static HashMap<String,String> StoreClassificationValuesByValue(List<List<String>> list,List<List<Double>> convertedList){
        HashMap<String,String> tempHashMap = new HashMap<>();
        for(int i=0; i<list.size(); i++){
            List doubleValues;
            String val="";
            for(int j=0; j<list.get(0).size()-1; j++){
                doubleValues = list.get(i);
                val= val + (String) doubleValues.get(j);
                if(j==list.get(0).size()-2){
                    tempHashMap.put(val,list.get(i).get(j+1));
                    // there could be a mistake while storing data but
                    // hashmap displays the result as i want to see
                    // so do not touch here...
                }
            }
        }

        return tempHashMap;
    }

    static void AddNewRecord(List<List<Double>> records, int k, List<Double>newRecord){
        for(int i=0; i<records.size(); i++){
            if(kNeighbours.size()<k){
                double tempsum = 0;
                for(int j=0; j<records.get(i).size(); j++){
                    tempsum+=Eucledian(records.get(i).get(j),newRecord.get(j));
                }
                kNeighbours.add(i,records.get(i));
                kNSums.add(i,tempsum);
            }
            else{
                CalculateDistance(records.get(i),newRecord,i,k);
            }
        }
    } // let's calculate the distances to our new record

    static double CalculateDistance(List<Double> x, List<Double>y, int index,int k){ // new record = y
        double sum=0;
        for(int i=0; i<y.size(); i++){
            sum = sum + Eucledian(x.get(i), y.get(i));
        }
        CheckList(x,k,sum);
        return 0;

    } // a wider and upper loop. probably you can make it simple in other methods

    static void CheckList(List<Double> list, int K,Double cdSum){
        int index = -1;
        double sum = cdSum;
        double tempSum = 0;
            for(int i=0; i<K; i++){
                for(int j=0; j<list.size(); j++){
                    tempSum = tempSum+Eucledian(list.get(j),kNeighbours.get(i).get(j));
                }
                if(sum<tempSum){
                    tempSum = sum;
                    index = i;
                }
                if(index != -1){
                    kNeighbours.remove(index);
                    kNeighbours.add(list);
                }

        }
    } // check knn neighbours

    static double Eucledian(double x, double y){
        double sum = x-y;
        sum = sum*sum;
        return sum;
    } // Eucledian distance

    static void getClass(List<List<Double>> list, HashMap<String,String> classes,String[] firstRow,List<Double>newRecord){
        String[] Frequency = new String[list.size()];
        String bd = null;
        for(int i=0; i<list.size(); i++){
            for(int j=0; j<list.get(0).size(); j++){
                String builder = String.valueOf(list.get(i).get(j));
                if(bd==null){
                    bd = builder;
                }
                else{
                    bd+=builder;
                }
            }
            String classOfValue = classes.get(bd);
            Frequency[i] = classOfValue;
            System.out.println(Frequency[i]);
            bd=null;
        }
        String key = findMostFrequentWord(Frequency);
        String[] uniqueClassNames = Arrays.stream(Frequency).distinct().toArray(String[]::new);
        System.out.println("Most frequent: " + key);

        classByWeights(list,classes,uniqueClassNames,newRecord);
    } // determine class name

    static String findMostFrequentWord(String[] arr){

            // Create HashMap to store word and it's frequency
            HashMap<String, Integer> hs = new HashMap<String, Integer>();

            // Iterate through array of words
            for (int i = 0; i < arr.length; i++) {
                // If word already exist in HashMap then increase it's count by 1
                if (hs.containsKey(arr[i])) {
                    hs.put(arr[i], hs.get(arr[i]) + 1);
                }
                // Otherwise add word to HashMap
                else {
                    hs.put(arr[i], 1);
                }
            }

            // Create set to iterate over HashMap
            Set<Map.Entry<String, Integer> > set = hs.entrySet();
            String key = "";
            int value = 0;

            for (Map.Entry<String, Integer> me : set) {
                // Check for word having highest frequency
                if (me.getValue() > value) {
                    value = me.getValue();
                    key = me.getKey();
                }
            }

            // Return word having highest frequency
            return key;
    } // find most frequent word by distances

    static String builderForDouble(List<Double> list){
        String bd = null;
        for(int i=0; i<list.size(); i++){
            String builder = String.valueOf(list.get(i));
            if(bd == null){
                bd  = builder;
            }
            else{
                bd+=builder;
            }
        }

        return bd;
    } // it builds a string in order to find its class. copied it from getClass and made it a method

    static void classByWeights(List<List<Double>> list,HashMap<String,String> classes,String[] firstRow,List<Double> newRecord){
        double sum = 0;
        HashMap<String,Double> weightsAndClasses = new HashMap<>();
        // initialize hashmap to store our sums
        for(int i=0; i<firstRow.length; i++){
            weightsAndClasses.put(firstRow[i],Double.valueOf(0));
        }

        for(int i=0; i<kNeighbours.size(); i++){
            for(int j=0; j<kNeighbours.get(0).size(); j++){
                sum = sum + Eucledian(kNeighbours.get(i).get(j),newRecord.get(j));
            }
            String doubleValueInString = builderForDouble(list.get(i));
            String classOfValue = classes.get(doubleValueInString);
            double tempSum = weightsAndClasses.get(classOfValue);
            tempSum = tempSum + ((1/sum) * (1/sum));
            weightsAndClasses.put(classOfValue,tempSum);
            sum=0;
        }
        double tempSum = -1;
        int index = -1;
        for(int i=0; i<firstRow.length; i++){
            sum = weightsAndClasses.get(firstRow[i]);
            if(tempSum == -1){
                tempSum = sum;
                index = i;
            }
            else if(sum>tempSum){
                tempSum = sum;
                index = i;
            }
        }
        for(Map.Entry<String,Double> entry: weightsAndClasses.entrySet()){
            System.out.println("Sum For: " + entry.getKey() + " And sum is : " + entry.getValue());
        }

        System.out.println("Class by weighted  distances of new record is " + firstRow[index] + " and our record is: " + newRecord);

    } // finally calculate the weighted euclidean distance

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}

class ChooseFile {
    private JFrame frame;
    public ChooseFile() {
        frame = new JFrame();

        frame.setVisible(true);
        BringToFront();
    }
    public File getFile() {
        JFileChooser fc = new JFileChooser();
        if(JFileChooser.APPROVE_OPTION == fc.showOpenDialog(null)){
            frame.setVisible(false);
            return fc.getSelectedFile();
        }else {
            System.out.println("Next time select a file.");
            System.exit(1);
        }
        return null;
    }

    private void BringToFront() {
        frame.setExtendedState(JFrame.ICONIFIED);
        frame.setExtendedState(JFrame.NORMAL);

    }

}