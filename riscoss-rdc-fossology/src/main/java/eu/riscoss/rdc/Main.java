package eu.riscoss.rdc;



public class Main
{
    public static void main(String[] args) throws Exception
    {
        RDCRunner.exec(args, new FossologyRiskDataCollector());
    }
}
