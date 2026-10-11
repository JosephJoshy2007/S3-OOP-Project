import java.util.List;

// Module 6: Generic Searchable Contract
public interface Searchable<T> {
    List<T> search(String query);
}
