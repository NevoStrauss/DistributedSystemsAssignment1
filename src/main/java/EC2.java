import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EC2 {
    private final Ec2Client ec2;
    private final String keyName;
    private final String arn;

    public EC2(String keyName, String arn, Region region) {
        ec2 = Ec2Client.builder().region(region).build();
        this.keyName = keyName;
        this.arn = arn;
    }

    public Ec2Client getEc2() {
        return ec2;
    }

    public void startInstance(String instanceId){
        StartInstancesRequest startRequest = StartInstancesRequest.builder()
                .instanceIds(instanceId).build();
        ec2.startInstances(startRequest);
    }

    public void stopInstance(String instanceId){
        StopInstancesRequest request = StopInstancesRequest.builder()
                .instanceIds(instanceId).build();
        ec2.stopInstances(request);
    }


    public void terminateInstance(String instanceId) {
        TerminateInstancesRequest request = TerminateInstancesRequest.builder()
                .instanceIds(instanceId).build();
        ec2.terminateInstances(request);
    }

    public String getManager(String amiId){ //TODO: Need to add data parameter
        Filter filter = Filter.builder()
                .name("instance-state-name")
                .values("running", "stopped")
                .build();

        System.out.println("Build Describe request");

        DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                .filters(filter)
                .build();
        String nextToken = null;

        System.out.println("Start list all available instances");
        do {
            DescribeInstancesResponse response = ec2.describeInstances(request);

            for(Reservation reservation : response.reservations()) {
                for(Instance instance : reservation.instances()) {
                    System.out.printf("%s", instance.state().name());
                    for (Tag tag: instance.tags()) {
                        if (tag.key().equals("manager")){
                            if(instance.state().name().toString().equals("running") || instance.state().name().toString().equals("pending")){
                                return instance.instanceId();
                            }
                            else if(instance.state().name().toString().equals("stopped")){
                                startInstance(instance.instanceId());
                                return instance.instanceId();
                            }
                        }
                    }
                }
            }

            // Creating a manager
            nextToken = response.nextToken();

        } while(nextToken != null);

        return createManagerInstance(amiId);
    }

    private String createManagerInstance(String amiId) { //TODO: Need to add data parameter
        RunInstancesRequest runRequest = RunInstancesRequest.builder()
                .instanceType(InstanceType.T2_MICRO)
                .imageId(amiId)
                .keyName(keyName)
                .maxCount(1)
                .minCount(1)
                .securityGroups("launch-wizard-5")
                .userData(get_script())
                .iamInstanceProfile(IamInstanceProfileSpecification.builder().arn(arn).build())
                .build();

        RunInstancesResponse response = ec2.runInstances(runRequest);

        String instanceId = response.instances().get(0).instanceId();

        Tag tag = Tag.builder()
                .key("manager")
                .value("manager")
                .build();

        CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        try {
            ec2.createTags(tagRequest);
            System.out.printf(
                    "Successfully started EC2 instance %s based on AMI %s",
                    instanceId, amiId);

        } catch (Ec2Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        return instanceId;
    }

    private String get_script() {
        String script = "#!/bin/bash\n";
        script += "sudo mkdir jars\n";
        script += "cd jars\n";
        script += "sudo aws s3 cp s3://bucketqoghawn0ehuw2njlvyexsmxt5dczxfwc/Manager.jar ./\n";
        script += "sudo java -Xmx30g -jar ./Manager.jar ami-0878fb723a9a1c5db " + keyName + " " +  arn;

        return new String(Base64.getEncoder().encode(script.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

}
