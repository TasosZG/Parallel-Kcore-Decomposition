import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Park extends KcoreComputer
{
    private List<Long> curr;
    private List<Long> next;

    public Park(Map<Long, ArrayList<Long>> adj,Map<Long, AtomicInteger> deg, int num_threads)
    {
        super(adj,deg,num_threads);
    }

    private class Park_worker extends Thread
    {
        private List<Long> partition;
        private int level;
        private LinkedList<Long> buff;
        private boolean scan;//to scan or to sub_process
        public Park_worker(String name, int l, List<Long> partition, boolean scan)
        {
            setName(name);
            this.partition = partition;
            level = l;
            buff = new LinkedList<>();
            this.scan = scan;
        }

        @Override
        public void run()
        {
            if(scan) doScan(); else doProcess_sub_level();
        }
        private void doProcess_sub_level()
        {
            for (Long node: partition)
            {
                for (Long neighbour: adj.get(node))
                {
                    if(deg.get(neighbour).get() > level)
                    {
                        int deg_of_neighbour = deg.get(neighbour).decrementAndGet();
                        if (deg_of_neighbour == level) buff.add(neighbour);
                        if (deg_of_neighbour < level) deg.get(neighbour).incrementAndGet();
                    }
                }
            }
            next.addAll(buff);
        }
        private void doScan()
        {
            for (Long node : partition)
            {
                if (deg.get(node).get() == level)
                {
                    buff.add(node);
                }
            }
            curr.addAll(buff);
        }
    }
    //True to fork scan, false to fork sub_level
    private void fork(int l,List<Long> nodes,boolean scan)
    {
        int size = nodes.size();//Number of elements, not total capacity
        int step = size/num_threads;
        int start = 0;
        int end = step;

        //Start all but the last thread
        for (int i = 0; i < num_threads - 1; i++)
        {
            threads[i] = new Park_worker("Thread " + i,l, nodes.subList(start,end),scan);
            threads[i].start();
            start = end;
            end+=step;
        }
        //Start last thread by giving all the remaining allNodes
        threads[num_threads-1] = new Park_worker("Thread N",l,  nodes.subList(start,size),scan);
        threads[num_threads-1].start();
        //Wait for threads to finish
        for (Thread thread : threads) try { thread.join(); } catch (Exception e) { e.printStackTrace(); }
    }

    private void scan(int l,List<Long> nodes)
    {
        fork(l,nodes,true);
    }
    private void subLevel(int l,List<Long> nodes)
    {
        fork(l, nodes,false);
    }
    @Override
    public void execute()
    {
        System.out.println("Running PARK");
        curr = Collections.synchronizedList(new ArrayList<>(totalNodes));//Size totalNodes for performance
        next = Collections.synchronizedList(new ArrayList<>(totalNodes));
        int todo = totalNodes;
        int level = 1;

        while(todo > 0)
        {
            scan(level, allNodes);//Synchronization inside
            while(!curr.isEmpty())
            {
                todo-=curr.size();
                //PROCESS SUB LEVEL
                subLevel(level,curr);//Synchronization inside
                curr = Collections.synchronizedList(new ArrayList<>(next));
                next.clear();
                for (Thread thread : threads) try { thread.join(); } catch (Exception e) { e.printStackTrace(); }
            }
            ++level;
        }
    }
}
