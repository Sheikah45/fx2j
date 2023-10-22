package fx2j.builder;

import fx2j.builder.com.example.sample.HelloViewBuilder;
import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import java.lang.String;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Fx2jBuilderFinder implements io.github.sheikah45.fx2j.api.Fx2jBuilderFinder {
  private static final HashMap<String, Supplier<? extends Fx2jBuilder<?, ?>>> BUILDER_PATH_MAP = new HashMap<>(2, 0.75f);

  static {
    BUILDER_PATH_MAP.put("com/example/sample/hello-view.fxml", HelloViewBuilder::new);
  }

  public Fx2jBuilder<?, ?> findBuilder(URL location) {
    if (location == null) {
      return null;
    }
    String path = location.getPath();
    return BUILDER_PATH_MAP.entrySet().stream().filter(entry -> path.endsWith(entry.getKey())).map(Map.Entry::getValue).map(Supplier::get).findFirst().orElse(null);
  }
}
