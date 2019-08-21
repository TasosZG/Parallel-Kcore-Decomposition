import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class PKC extends KcoreComputer
{
    private CyclicBarrier barrier;
    private AtomicInteger visited;
    private Double fraction;
    private int trigger_cleanup_limit;

    private Set<Long> remaining;

    public PKC(Map<Long, ArrayList<Long>> adj, Map<Long, AtomicInteger> deg, int num_threads, double pc)
    {
        super(adj,deg,num_threads);
        fraction = pc * totalNodes;
        trigger_cleanup_limit = fraction.intValue();
        remaining = ConcurrentHashMap.newKeySet();
    }

    public class PKC_worker extends Thread {
        private LinkedList<Long> partition;
        private int l;
        private int s;
        private int e;
        private boolean triggered = false;

        public PKC_worker(String name, LinkedList<Long> partition) {
            setName(name);
            this.partition = partition;
            l = 1;
            s = 0;
            e = 0;
        }

        @java.lang.Override
        public void run() {
            ArrayList<Long> buff = new ArrayList<>(partition.size());
            Long next;
            ListIterator<Long> it;
            do {
                buff.clear();
                it = partition.listIterator();
                while(it.hasNext())
                {
                    next = it.next();
                    if(deg.get(next).get() == l)
                    {
                        buff.add(next);
                        e++;
                        it.remove();
                    }
                }
                try {barrier.await();} catch (Exception e) {e.printStackTrace();}
                while (s < e) {
                    for (Long neighbour : adj.get(buff.get(s))) {
                        if (deg.get(neighbour).get() > l) {
                            int deg_of_neighbour = deg.get(neighbour).decrementAndGet();
                            if (deg_of_neighbour == l) {
                                buff.add(neighbour);
                                e++;
                            }
                            if (deg_of_neighbour < l) deg.get(neighbour).incrementAndGet();
                        }
                    }
                    ++s;
                }

                try {barrier.await();} catch (Exception e) {e.printStackTrace();}
                visited.addAndGet(e);
                try {barrier.await();} catch (Exception ex) {ex.printStackTrace();}
                //If 98% visited, add remaining to set and remove from adj
                if(!triggered && visited.get() >= trigger_cleanup_limit)
                {
                    triggered = true;
                    remaining.addAll(partition);
                    try {barrier.await();} catch (Exception e) {e.printStackTrace();}
                    for (Long node: partition)
                    {
                        adj.get(node).retainAll(remaining);//remove visited nodes from adjacency list
                    }
                }
                //
                ++l;
                e = 0;
                s = 0;
            } while (visited.get() < totalNodes);

        }
    }

    @Override
    public void execute()
    {
        barrier = new CyclicBarrier(num_threads);
        visited = new AtomicInteger();
        System.out.println("Started PKC");
        Thread[] threads = new Thread[num_threads];

        for (int i = 0; i < num_threads; i++)
        {
            threads[i] = new PKC_worker("Thread " + i, sublists.get(i));
            threads[i].start();
        }

        System.out.println("Started " + num_threads + "threads");
        //Wait for threads to finish
        for (Thread thread : threads)try { thread.join(); } catch (Exception e) { e.printStackTrace();}
    }
}
