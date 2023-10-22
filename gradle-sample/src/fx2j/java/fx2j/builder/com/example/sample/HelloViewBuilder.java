package fx2j.builder.com.example.sample;

import com.example.sample.HelloController;
import io.github.sheikah45.fx2j.api.Fx2jBuilder;
import java.lang.Class;
import java.lang.Object;
import java.util.ResourceBundle;
import java.util.function.Function;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class HelloViewBuilder implements Fx2jBuilder<HelloController, VBox> {
  private HelloController controller;

  private VBox root;

  public HelloController getController() {
    return controller;
  }

  private void setController(HelloController builderProvidedController,
      Function<Class<?>, Object> controllerFactory) {
    if (builderProvidedController != null) {
      controller = builderProvidedController;
    } else if (controllerFactory != null) {
      controller = (HelloController) controllerFactory.apply(HelloController.class);
    } else {
      controller = new HelloController();;
    }
  }

  public VBox getRoot() {
    return root;
  }

  private void setRoot(VBox root) {
    this.root = root;
  }

  public void build(HelloController builderProvidedController, VBox builderProvidedRoot,
      ResourceBundle resources, Function<Class<?>, Object> controllerFactory) {
    setController(builderProvidedController, controllerFactory);

    VBox vBox0 = new VBox();
    ObservableList<Node> vBox0Children = vBox0.getChildren();

    Label welcomeText = new Label();
    controller.welcomeText = welcomeText;
    welcomeText.setId("welcomeText");
    welcomeText.setTextAlignment(TextAlignment.CENTER);

    vBox0Children.add(welcomeText);

    Button button0 = new Button();
    button0.setText("Hello!");
    button0.setOnAction(event -> controller.onHelloButtonClick());

    vBox0Children.add(button0);
    vBox0.setSpacing(20.0);
    vBox0.setAlignment(Pos.CENTER);

    Insets insets0 = new Insets(20.0, 20.0, 20.0, 20.0);

    vBox0.setPadding(insets0);

    setRoot(vBox0);
  }
}
