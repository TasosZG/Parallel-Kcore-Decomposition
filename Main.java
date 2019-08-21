import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static Map<Long, ArrayList<Long>> adj;
    private static Map<Long, AtomicInteger> deg;
    private static ArrayList<Long> currentList;
    private static AtomicInteger currentDeg;
    private static KcoreComputer computer;

    private static void addNeighbour(Long left, Long right) {
        currentList = adj.get(left);
        currentDeg = deg.get(left);
        if (currentList == null) //both should be null
        {
            currentList = new ArrayList<>();
            adj.put(left, currentList);
            currentDeg = new AtomicInteger();
            deg.put(left, currentDeg);
        }
        currentList.add(right);
        currentDeg.incrementAndGet();
    }

    public static void main(String[] args)
    {
        double pc = 1.1;
        boolean sort = false;
        boolean test = false;
        boolean pkc = false;
        String inputFilename = "sample.txt";
        int num_threads = 1;
        boolean write_deg = true;
        String outputFilename = "core-list.txt";
        if(args.length > 0)
            if(args[0].equals("pkc")) pkc = true;
            else if(args[0].equals("park")) pkc = false;
            else if(args[0].equals("test")) test = true;
            else {System.out.println("First argument pkc or park"); return;}
        if(args.length > 1) inputFilename = args[1]; 
        if(args.length > 2) num_threads = Integer.parseInt(args[2]);
        if(args.length > 3) if(args[3].equals("nowrite")){ System.out.println("no output"); write_deg = false;} else outputFilename = args[3];
        if(args.length > 4) { pc = Double.parseDouble(args[4]); System.out.println("Percentage = " + pc);}
        adj = new HashMap<>();
        deg = new HashMap<>();
        long before_read = System.currentTimeMillis();

	if(inputFilename.endsWith(".txt")) try {read_file(inputFilename);}catch (IOException e) { e.printStackTrace(); return;}

        System.out.println("Read file in " + (System.currentTimeMillis() - before_read)/1000 + " seconds");

        if(pkc) computer = new PKC( adj,deg,num_threads,pc);
        else    computer = new Park(adj,deg,num_threads);

        if(test){write_deg_text(outputFilename); return;}//Testing

        System.out.println("Starting Process");
        long begin = System.currentTimeMillis();

        if(pkc && sort)
        {
            computer.sort();
            System.out.println("Sorted in " + (System.currentTimeMillis() - begin)/1000 + " seconds");

        }else {computer.nosort();}
        begin = System.currentTimeMillis();
        computer.execute();

        long finish = System.currentTimeMillis();
        System.out.println("Finished in " + (finish - begin)/1000 + " seconds");

        //Write output
        if(write_deg)
            if(outputFilename.endsWith(".txt")) write_deg_text(outputFilename);

        System.out.println("MAIN FINISHED input was: " + inputFilename + " threads: " + num_threads);
    }



    private static void write_deg_text(String outputFilename)
    {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilename)))
        {
            String line_to_write;
            ArrayList<Long> allNodes = computer.getAllNodes();
            for(Long node : allNodes)
            {
                line_to_write = node + "\t" + deg.get(node).get();
                writer.write(line_to_write);
                writer.newLine();
            }
            Long max = allNodes.get(0);
            System.out.println("Max entry was: " + max + " : " + deg.get(max).get());
            System.out.println("Wrote deg to " + outputFilename);
        }catch (Exception e){e.printStackTrace();}
    }

    private static void read_file(String inputFilename) throws IOException
    {
        System.out.println("Reading from text file: " + inputFilename);
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFilename)))
        {
            long left,right;
            String line;
            String[] tokens;
            while( ( line = reader.readLine() ) != null)
            {
                if(line.startsWith("#") || line.isEmpty()) continue;
                
		tokens = line.split("\\t");
                left = Long.parseLong(tokens[0]);
                right = Long.parseLong(tokens[1]);
                addNeighbour(left,right);
                addNeighbour(right,left);
            }
        }

    }

}
