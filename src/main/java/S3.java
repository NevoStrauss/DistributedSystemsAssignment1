import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import java.io.File;
import java.util.List;

public class S3 {
    private final S3Client s3;
    private final String bucketName;
    private final String queueName;
    private final PdfConverter pdfConverter;
    private CreateBucketResponse bucketResponse;

    public S3(String bucketName, String queueName, Region region, PdfConverter pdfConverter) {
        this.s3 = S3Client.builder()
                .region(region)
                .build();

        this.bucketName = bucketName;
        this.queueName = queueName;
        this.pdfConverter = pdfConverter;
    }

    public String getBucketKey() {
        return bucketResponse.location();
    }

    public CreateBucketResponse createBucket() {
        bucketResponse = s3.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        return bucketResponse;
    }

    public void putObject(String filePath, String bucketKey) {
        s3.putObject(PutObjectRequest.builder().bucket(bucketName).key(bucketKey).build(),
                RequestBody.fromFile(new File(filePath)));
    }


    public ResponseInputStream<GetObjectResponse> getObject() {
        return s3.getObject(GetObjectRequest.builder().bucket(bucketName).key(queueName).build());
    }

    public void terminate() {
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(queueName).build());
        s3.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
    }

    public void uploadJars(){
        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucketName)
                    .build();

            ListObjectsResponse res = s3.listObjects(listObjects);
            List<S3Object> objects = res.contents();
            boolean manager = false, worker = false;

            for (S3Object s3Object : objects) {
                if (s3Object.key().equals("Manager.jar"))
                    manager = true;
                if (s3Object.key().equals("Worker.jar"))
                    worker = true;
            }

            if(!manager){
                putObject(pdfConverter.getInputFileName(), "Manager.jar");
            }
            if(!worker){
                putObject(pdfConverter.getInputFileName(), "Worker.jar");
            }

        } catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

}

