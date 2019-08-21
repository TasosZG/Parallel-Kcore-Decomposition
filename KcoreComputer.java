import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class KcoreComputer
{
    Map<Long, ArrayList<Long>> adj;
    ArrayList<Long> allNodes;
    Map<Long, AtomicInteger> deg;
    int totalNodes;
    int num_threads;
    Thread[] threads;

    ArrayList<LinkedList<Long>> sublists;

    public KcoreComputer(Map<Long, ArrayList<Long>> adj,Map<Long, AtomicInteger> deg, int num_threads)
    {
        this.adj = adj;
        allNodes = new ArrayList<>(this.adj.keySet());
        this.deg = deg;
        this.num_threads = num_threads;
        totalNodes = adj.size();
        threads = new Thread[num_threads];
    }

    abstract public void execute();

    public void random_shuffle()
    {
        Collections.shuffle(allNodes);
    }
    public void nosort()
    {
        sublists = new ArrayList<>();

        for (int i = 0; i < num_threads; i++)
        {
            sublists.add(new LinkedList<>());
        }

        int step = totalNodes/num_threads;
        int start = 0;
        int end = step;

        for (int i = 0; i < num_threads - 1; i++)
        {
            sublists.get(i).addAll(allNodes.subList(start,end));
            start = end;
            end+=step;
        }
        sublists.get(num_threads-1).addAll(allNodes.subList(start,totalNodes));
    }

    public void sort()
    {

        Comparator<Long> comparator = (o1,o2)->deg.get(o2).get() - deg.get(o1).get();
        allNodes.sort(comparator);

        sublists = new ArrayList<>();

        for (int i = 0; i < num_threads; i++)
        {
            sublists.add(new LinkedList<>());
        }

        for (int i = 0; i < allNodes.size(); i++)
        {
            sublists.get(i%num_threads).add(allNodes.get(i));
        }

    }

    public ArrayList<Long> getAllNodes()
    {
        Comparator<Long> comparator = (o1,o2)->deg.get(o2).get() - deg.get(o1).get();
        allNodes.sort(comparator);
        return allNodes;
    }

}
