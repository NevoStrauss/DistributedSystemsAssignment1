import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

public class SQS {
    private final SqsClient sqs;
    private final String queueName;
    private final String queueUrl;

    public SQS(String queueName, Region region) {
        this.sqs = SqsClient.builder().region(region).build();
        this.queueName = queueName;
        this.queueUrl = getUrl(createQueue());
    }

    public String getQueueUrl() {return queueUrl;}

    private CreateQueueResponse createQueue() {
        return sqs.createQueue(CreateQueueRequest.builder().queueName(queueName).build());
    }

    private String getUrl(CreateQueueResponse createQueueResponse) {
        return createQueueResponse.queueUrl();
    }

    public void sendMessage(String msg) {
        sqs.sendMessage(SendMessageRequest.builder().queueUrl(queueUrl).messageBody(msg).build());
    }

    public List<Message> receiveMessages() {
        return sqs.receiveMessage(ReceiveMessageRequest.builder().queueUrl(queueUrl).build()).messages();
    }

    public void terminate(List<Message> messages) {
        for (Message m : messages) {
            sqs.deleteMessage(DeleteMessageRequest.builder().queueUrl(queueUrl).receiptHandle(m.receiptHandle()).build());
            sqs.deleteQueue(DeleteQueueRequest.builder().queueUrl(queueUrl).build());
        }
    }

    public void deleteQueue(String queueUrl){
        DeleteQueueRequest deleteQueueRequest = DeleteQueueRequest.builder()
                .queueUrl(queueUrl)
                .build();

        sqs.deleteQueue(deleteQueueRequest);
    }

}


