package rtandroid.benchmark.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores the result of a benchmark.
 */
public class BenchmarkResult implements Parcelable
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
    private final Map<String, Map<Kind, Integer>> mResults = new LinkedHashMap<>();

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

    public Map<String, Integer> getResult(Kind kind)
    {
        Map<String, Integer> results = new LinkedHashMap<>();

        for(Map.Entry<String, Map<Kind, Integer>> e : mResults.entrySet())
        {
            String name = e.getKey();
            Integer value = e.getValue().get(kind);
            results.put(name, value);
        }

        return results;
    }

    //
    // Parcelable
    //

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(mName);

        // Just write all elements down, sizes first for reading
        dest.writeInt(mResults.size());
        for(Map.Entry<String, Map<Kind, Integer>> caseEntry : mResults.entrySet())
        {
            dest.writeString(caseEntry.getKey());
            Map<Kind, Integer> caseResults = caseEntry.getValue();
            dest.writeInt(caseResults.size());
            for(Map.Entry<Kind, Integer> kindEntry : caseResults.entrySet())
            {
                dest.writeString(kindEntry.getKey().name());
                dest.writeInt(kindEntry.getValue());
            }
        }
    }

    public static final Parcelable.Creator<BenchmarkResult> CREATOR = new Parcelable.Creator<BenchmarkResult>()
    {
        public BenchmarkResult createFromParcel(Parcel in)
        {
            return new BenchmarkResult(in);
        }

        public BenchmarkResult[] newArray(int size)
        {
            return new BenchmarkResult[size];
        }
    };

    private BenchmarkResult(Parcel in) {
        mName = in.readString();

        // Read all elements, use provided sizes
        int resultsCount = in.readInt();
        for(int caseIdx = 0; caseIdx < resultsCount; caseIdx++)
        {
            String name = in.readString();
            Map<Kind, Integer> results = new LinkedHashMap<>();
            int caseResult = in.readInt();
            for(int resultIdx = 0; resultIdx < caseResult; resultIdx++)
            {
                Kind kind = Kind.valueOf(in.readString());
                int value = in.readInt();
                results.put(kind, value);
            }
            mResults.put(name, results);
        }
    }
}
