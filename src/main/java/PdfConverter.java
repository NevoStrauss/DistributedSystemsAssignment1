public class PdfConverter {

    private String inputFileName;
    private String outputFileName;
    private int numOfPdfPerWorker;
    private Boolean shouldTerminate;

    public PdfConverter(String[] args) {
        this.inputFileName = args[0];
        this.outputFileName = args[1];
        this.numOfPdfPerWorker = Integer.parseInt(args[2]);
        this.shouldTerminate = args.length >= 4;
    }

    public String getInputFileName() {return inputFileName;}
    public String getOutputFileName() {return outputFileName;}
    public int getNumOfPdfPerWorker() {return numOfPdfPerWorker;}
}
