package com.example.cities;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import java.io.*;
import java.util.Random;


public class MachineLearningCrossValidation {

    String tanito = "";
    String kiertekelo = "";
    String tp = "";
    String tn = "";
    String fp = "";
    String fn = "";
    String cci = "";
    String ici = "";
    private String cci_pct = "";

    public String getCci_pct() {
        return cci_pct;
    }

    public void setCci_pct(String cci_pct) {
        this.cci_pct = cci_pct;
    }

   public MachineLearningCrossValidation(String fájlNév, int classIndex, Classifier classifier){	// Classifier classifier: interface
        // https://weka.sourceforge.io/doc.dev/weka/classifiers/Classifier.html
        System.out.println("\nclassifier.getClass():"+classifier.getClass());
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fájlNév));
            Instances instances = new Instances(bufferedReader);
            instances.setClassIndex(classIndex);
            Instances tanító, kiértékelő;
            if(false) instances.randomize(new Random());
            Evaluation evaluation = new Evaluation(instances);
            evaluation.crossValidateModel( classifier, instances, 10, new Random(1));
            // 10: 10-szeres keresztvalidáció
            // new Random(1): véletlenszám generátor
            System.out.println(evaluation.toSummaryString("\nResults", false));

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
            String fileba = classifier + "\n" + "Tanító halmaz mérete: " + tanito + "\n" +  "kiértékelő halmaz mérete " + kiertekelo + "\n" + "TP(TruePositive): " + tp + "\n" + "TP(TrueNegative): " + tn + "\n" + "FP(FalsePositive): " + fp + "\n" + "FP(FalseNegative): " + fn + "\n" + "Correctly Classified Instances: " + cci + "\n" + "Incorrectly Classified Instances: "+ ici + "\n" ;
            System.out.println(fileba);
            Writer output;
            output = new BufferedWriter(new FileWriter("data/Gépi tanulás.txt" , true));  //clears file every time
            output.append(fileba);
            output.close();
            cci_pct = String.valueOf(100*evaluation.correct()/instances.size());
            System.out.println("Correctly Classified Instances:"+(int)evaluation.correct()+"\t"+100*evaluation.correct()/instances.size()+"%");
            System.out.println("Incorrectly Classified Instances:"+(instances.size()-(int)evaluation.correct()));
            System.out.println();
        }
        catch (Exception e) {
            System.out.println("Error Occurred!!!! \n" + e.getMessage());
        }
    }


}



