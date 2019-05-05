package io.mcomputing.activitymonitoring.Helpers;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class FileManager {

	//read training files
	public static TestRecord createNewTestRecord(String testFeature){

		String[] splittedData = testFeature.split(",");
		double[] attributes = new double[6]; // 6 attributes
		int counter = 0;
		for(String featureNr: splittedData){
			attributes[counter] = Double.parseDouble(featureNr);
			counter++;
		}
		return new TestRecord(attributes, -1);

	}
	public static TrainRecord[] readTrainFile(String fileName) {
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(fileName));
			line = br.readLine();
			String[] firstLine = line.split(cvsSplitBy);

			int NumOfSamples = Integer.parseInt(firstLine[0]);
			int NumOfAttributes = Integer.parseInt(firstLine[1]);

			int LabelOrNot = Integer.parseInt(firstLine[2]);

			assert LabelOrNot == 1 : "No classLabel";// ensure that C is present in this file
			TrainRecord[] records = new TrainRecord[NumOfSamples];
			int index = 0;
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] splitted_line = line.split(cvsSplitBy);


				double[] attributes = new double[NumOfAttributes];
				int classLabel = -1;

				//Read a whole line for a TrainRecord
				for(int i = 0; i < NumOfAttributes; i ++){
					attributes[i] = Double.parseDouble(splitted_line[i]);
				}

				//Read classLabel
				classLabel = Integer.parseInt(splitted_line[splitted_line.length - 1]);
				assert classLabel != -1 : "Reading class label is wrong!";

				records[index] = new TrainRecord(attributes, classLabel);
				index ++;
			}

			return records;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	/*public static TrainRecord[] readTrainFile(String fileName) throws IOException{
		File file = new File(fileName);
		Scanner scanner = new Scanner(file).useDelimiter(",");

		//read file
		int NumOfSamples = scanner.nextInt();
		int NumOfAttributes = scanner.nextInt();

		int LabelOrNot = scanner.nextInt();
		Log.d("GIVMENUMBE", "s:" + NumOfSamples + " att:" + NumOfAttributes + " la:" + LabelOrNot);
		scanner.nextLine();

		assert LabelOrNot == 1 : "No classLabel";// ensure that C is present in this file


		//transform data from file into TrainRecord objects
		TrainRecord[] records = new TrainRecord[NumOfSamples];
		int index = 0;
		while(scanner.hasNext()){
			double[] attributes = new double[NumOfAttributes];
			int classLabel = -1;

			//Read a whole line for a TrainRecord
			for(int i = 0; i < NumOfAttributes; i ++){

				attributes[i] = scanner.nextFloat();
				Log.d("GIVMENUMBE", "att:" + attributes[i]);
			}

			//Read classLabel
			classLabel = (int) scanner.nextDouble();
			assert classLabel != -1 : "Reading class label is wrong!";

			records[index] = new TrainRecord(attributes, classLabel);
			index ++;
		}

		return records;
	}*/


	public static TestRecord[] readTestFile(String fileName) throws IOException{
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";

		try {

			br = new BufferedReader(new FileReader(fileName));
			line = br.readLine();
			String[] firstLine = line.split(cvsSplitBy);

			int NumOfSamples = Integer.parseInt(firstLine[0]);
			int NumOfAttributes = Integer.parseInt(firstLine[1]);

			int LabelOrNot = Integer.parseInt(firstLine[2]);

			assert LabelOrNot == 1 : "No classLabel";// ensure that C is present in this file
			TestRecord[] records = new TestRecord[NumOfSamples];
			int index = 0;
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] splitted_line = line.split(cvsSplitBy);


				double[] attributes = new double[NumOfAttributes];
				int classLabel = -1;

				//Read a whole line for a TrainRecord
				for(int i = 0; i < NumOfAttributes; i ++){

					attributes[i] = Double.parseDouble(splitted_line[i]);
				}

				//Read classLabel
				classLabel = Integer.parseInt(splitted_line[splitted_line.length - 1]);
				assert classLabel != -1 : "Reading class label is wrong!";

				records[index] = new TestRecord(attributes, classLabel);
				index ++;
			}

			return records;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static String outputFile(TestRecord[] testRecords, String trainFilePath) throws IOException{
		//construct the predication file name
		StringBuilder predictName = new StringBuilder();
		for(int i = 15; i < trainFilePath.length(); i ++){
			if(trainFilePath.charAt(i) != '_')
				predictName.append(trainFilePath.charAt(i));
			else
				break;
		}
		String predictPath = "classification\\"+predictName.toString()+"_prediction.txt";

		//ouput the prediction labels
		File file = new File(predictPath);
		if(!file.exists())
			file.createNewFile();

		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);

		for(int i =0; i < testRecords.length; i ++){
			TestRecord tr = testRecords[i];
			bw.write(Integer.toString(tr.predictedLabel));
			bw.newLine();
		}

		bw.close();
		fw.close();

		return predictPath;
	}
}