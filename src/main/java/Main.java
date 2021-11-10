import software.amazon.awssdk.regions.Region;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class Main {
    private static final String QUEUE_NAME = "dspqueue";
    private static final String BUCKET_NAME = "eranNevo";
    private static final Region REGION = Region.US_WEST_2;
    private static final String SECRETS = "key.txt";
    private static String key = "key" + new Date().getTime();
    private static String arn;
    private static String keyName;
    private static String instanceId;
    private static final String amiId = "ami-0878fb723a9a1c5db";


    public static void main(String[] args) {
        validateArgs(args);
        PdfConverter pdfConverter = new PdfConverter(args);
        S3 s3 = new S3(BUCKET_NAME, QUEUE_NAME, REGION, pdfConverter);
        SQS sqs = new SQS(QUEUE_NAME,REGION);

        getSecurityDetails();
        EC2 ec2 = new EC2(SECRETS, arn, REGION);

        s3.createBucket();
        s3.uploadJars();

        // Start manager if any exist, else crate new one
        System.out.println("Start manager if any exist, else crate new one");
        instanceId = ec2.getManager(amiId);

        // Upload file to s3 bucket
        System.out.println("Upload INput file file to s3 bucket");
        s3.putObject(pdfConverter.getInputFileName(), s3.getBucketKey());

        // Send message to Manager Queue
        System.out.println("Send message to Manager Queue");
        sqs.sendMessage("new_task " + pdfConverter.getNumOfPdfPerWorker() + " " + key + " " + sqs.getQueueUrl());

        // Get the queue url of the manager
        //getMessage();
        sqs.deleteQueue(sqs.getQueueUrl());
        System.out.println(System.nanoTime() - System.nanoTime());



        //terminateS3(s3);

        //pdfConverter.uploadFile();

    }

    private static void validateArgs(String[] args) throws RuntimeException {
        if (args.length < 3) throw new RuntimeException();
    }


    private static void getSecurityDetails(){
        File file = new File(SECRETS);
        try (BufferedReader bf = new BufferedReader(new FileReader(file))) {
            arn = bf.readLine();
            keyName = bf.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
