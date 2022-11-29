package com.example.cities;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Random;


public class MachineLearning {
    String tanito = "";
    String kiertekelo = "";
    String tp = "";
    String tn = "";
    String fp = "";
    String fn = "";
    String cci = "";
    String ici = "";
    MachineLearning(String fájlNév, int classIndex)
    {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fájlNév));
            Instances instances = new Instances(bufferedReader);
            System.out.println("instances.size():"+instances.size());
            instances.setClassIndex(classIndex);
            Instances tanító, kiértékelő;
            J48 classifier;
            Evaluation evaluation;
            classifier = new J48();
            System.out.println("\nGépiTanulás1: Randomize után vagy anélkül: tanító: első 90%, kiértékelő: utolsó 10%");
            if(false) instances.randomize(new Random());
            tanító = new Instances(instances,0,9*instances.size()/10);
            tanito = String.valueOf(tanító.size());
            System.out.println("tanító.size():"+tanító.size());
            kiértékelő = new Instances(instances,9*instances.size()/10,instances.size()/10);
            kiertekelo = String.valueOf(kiértékelő.size());
            System.out.println("kiértékelő.size():"+kiértékelő.size());
            classifier.buildClassifier(tanító);
            evaluation = new Evaluation(kiértékelő);
            double[] eredmeny = evaluation.evaluateModel(classifier, kiértékelő);
            System.out.println(evaluation.toSummaryString("\nResults", false));
            System.out.println("Correctly Classified Instances:"+(int)evaluation.correct());
            System.out.println("Incorrectly Classified Instances:"+(kiértékelő.size()-(int)evaluation.correct()));
            System.out.println(classifier.toString());

            System.out.println("\nGépiTanulás1: Vizsgálat részletesen, egyesével:");
            int TP=0, TN=0, FP=0, FN=0;
            //  TP:TruePositive, TN:TrueNegative, FP:FalsePositive, FN:FalseNegative
            for(int i=0;i<kiértékelő.size();i++){
                System.out.println(i+"\t"+(((Instances)kiértékelő).get(i)).classValue()+"\t"+eredmeny[i]);
                if((((Instances)kiértékelő).get(i)).classValue()==1 && eredmeny[i]==1)
                    TP++;
                if((((Instances)kiértékelő).get(i)).classValue()==1 && eredmeny[i]==0)
                    FN++;
                if((((Instances)kiértékelő).get(i)).classValue()==0 && eredmeny[i]==1)
                    FP++;
                if((((Instances)kiértékelő).get(i)).classValue()==0 && eredmeny[i]==0)
                    TN++;
            }
            tp = String.valueOf(TP);
            tn = String.valueOf(TN);
            fp = String.valueOf(FP);
            fn = String.valueOf(FN);
            System.out.println("TP="+TP+", "+"TN="+TN+", "+"FP="+FP+", "+"FN="+FN);
            System.out.println("TP+TN="+(TP+TN));
            System.out.println("FP+FN="+(FP+FN));
            System.out.println("\nGépiTanulás1: ha a tanító és kiértékelő megegyezik:");
            tanító = new Instances(instances,0,instances.size());
            kiértékelő = new Instances(instances,0,instances.size());
            classifier.buildClassifier(tanító);
            evaluation = new Evaluation(kiértékelő);
            evaluation.evaluateModel(classifier, kiértékelő);
            cci = String.valueOf(evaluation.correct());
            ici = String.valueOf(evaluation.incorrect());
            System.out.println(evaluation.toSummaryString("\nResults", false));
            String fileba = "Tanító halmaz mérete: " + tanito + "\n" +  "kiértékelő halmaz mérete " + kiertekelo + "\n" + "TP(TruePositive): " + tp + "\n" + "TP(TrueNegative): " + tn + "\n" + "FP(FalsePositive): " + fp + "\n" + "FP(FalseNegative): " + fn + "\n" + "Correctly Classified Instances: " + cci + "\n" + "Incorrectly Classified Instances: "+ ici;
            System.out.println(fileba);
            PrintWriter out = new PrintWriter("data/Döntési fa.txt");
            out.print(fileba);
            out.close();
        }
        catch (Exception e) {
            System.out.println("Error Occurred!!!! \n" + e.getMessage());


        }
    }
}
