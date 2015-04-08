package rtandroid.benchmark.data;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Stores the result of a benchmark.
 */
public class BenchmarkResult
{
    public enum Kind
    {
        CALCULATION_MINIMUM,
        CALCULATION_MAXIMUM,
        CALCULATION_MEAN,
        CALCULATION_DEVIATION,
        SLEEP_MINIMUM,
        SLEEP_MAXIMUM,
        SLEEP_MEAN,
        SLEEP_DEVIATION,
    }

    private final String mName;
    private final Map<String, Map<Kind, Integer>> mResults = new LinkedHashMap<String, Map<Kind, Integer>>();

    public BenchmarkResult(String name)
    {
        mName = name;
    }

    public void addResult(String testCase, Map<Kind, Integer> result)
    {
        mResults.put(testCase, result);
    }

    public String getName()
    {
        return mName;
    }

    public Set<String> getTestCases()
    {
        return mResults.keySet();
    }

    public Integer getResult(String testCase, Kind kind)
    {
        return mResults.get(testCase).get(kind);
    }

    public Map<String, Integer> getResult(Kind kind)
    {
        Map<String, Integer> results = new LinkedHashMap<String, Integer>();

        for(Map.Entry<String, Map<Kind, Integer>> e : mResults.entrySet())
        {
            String name = e.getKey();
            Integer value = e.getValue().get(kind);
            results.put(name, value);
        }

        return results;
    }
}
