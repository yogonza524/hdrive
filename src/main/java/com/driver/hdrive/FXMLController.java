package com.driver.hdrive;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.TickLabelOrientation;
import eu.hansolo.medusa.skins.ModernSkin;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.Duration;
import org.apache.commons.lang3.ArrayUtils;
import org.controlsfx.control.PopOver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.FormatUtil;
import oshi.util.Util;

//@Component
public class FXMLController implements Initializable {
    
    @FXML private Pane sensorPane;
    @FXML private VBox vBoxDisks;
    @FXML private HBox cpuToolBar;
    
    private PopOver diskPop;
    
    private Gauge temperatura;
    private HardwareAbstractionLayer hardware;
    private int procesadores;
    
    private final Image hd  = new Image("/img/hd.png");
    
    private Image[] listOfImages = {hd};
    private ListView<String> listDisks;
    private List<Label> cpuUsage;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        initConfig();
        initButtons();
        initListeners();
        initSensor();
        initList();
        initCpu();
    }    

    
    private void initConfig() {
        hardware = new SystemInfo().getHardware();
        procesadores = hardware.getProcessor().getLogicalProcessorCount();
        cpuUsage = new ArrayList<>();
        
        
        
        for (int i = 0; i < procesadores; i++) {
            Label l = new Label();
            Image img = new Image("/img/cpu.png");
            ImageView imagen = new ImageView(img);
            imagen.setFitHeight(32.0);
            imagen.setFitWidth(32.0);
            l.setGraphic(imagen);
            cpuUsage.add(l);
            cpuToolBar.getChildren().add(l);
        }
        
        if (sensorPane != null && sensorPane.getChildren().contains(temperatura)) {
                sensorPane.getChildren().remove(temperatura);
            }
        
        temperatura = new Gauge();  
            temperatura.setSkin(new eu.hansolo.medusa.skins.ModernSkin(temperatura));  
            temperatura.setTitle("Temperatura");  
            temperatura.setUnit("° C");  
            temperatura.setDecimals(0); 
            temperatura.setPrefWidth(sensorPane != null? sensorPane.getPrefWidth() : 200.0);
            temperatura.setPrefHeight(sensorPane != null? sensorPane.getPrefHeight(): 200.0);
            temperatura.setValueColor(Color.WHITE);  
            temperatura.setTitleColor(Color.WHITE);  
            temperatura.setUnitColor(Color.WHITE);
            temperatura.setSubTitleColor(Color.WHITE);  
            temperatura.setBarColor(Color.rgb(0, 214, 215));  
            temperatura.setNeedleColor(Color.WHITE);  
            temperatura.setThresholdColor(Color.rgb(204, 0, 0));  
            temperatura.setTickLabelColor(Color.rgb(151, 151, 151));  
            temperatura.setTickMarkColor(Color.GREY);  
            temperatura.setTickLabelOrientation(TickLabelOrientation.ORTHOGONAL);
            
            if (sensorPane != null) {
                sensorPane.getChildren().add(temperatura);
            }

            temperatura.setValue(new SystemInfo().getHardware().getSensors().getCpuTemperature());
    }

    private void initButtons() {
        
    }

    private void initListeners() {
        
    }

//    @Scheduled(fixedRate = 2000)
    private void initSensor() {
        if (temperatura == null) {
            initConfig();
        }
        new Thread(() -> {
            while(true){
                temperatura.setValue(new SystemInfo().getHardware().getSensors().getCpuTemperature());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private void initList() {
        listDisks = new ListView<>();
        
        HardwareAbstractionLayer hardware = new SystemInfo().getHardware();
        ObservableList<String> items =FXCollections.observableArrayList (
                );
        for(HWDiskStore disk : hardware.getDiskStores()){
            items.add(disk.getName());
        }
        
        
        listDisks.setItems(items);
        
        listDisks.setCellFactory(param -> {
                ListCell<String> cell = new ListCell<String>() {
            private final ImageView imageView = new ImageView();
            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    imageView.setImage(listOfImages[0]);
                    imageView.setFitHeight(32.0);
                    imageView.setFitWidth(32.0);
//                    else if(name.equals("APPLE"))
//                        imageView.setImage(listOfImages[1]);
//                    else if(name.equals("VISTA"))
//                        imageView.setImage(listOfImages[2]);
//                    else if(name.equals("TWITTER"))
//                        imageView.setImage(listOfImages[3]);
                    setText(name);
                    setGraphic(imageView);
                }
            }
        };
                
                cell.hoverProperty().addListener((obs, wasHovered, isNowHovered) -> {
                    if (isNowHovered && ! cell.isEmpty()) {
                        diskPop = new PopOver();
                        HWDiskStore disk = hardware.getDiskStores()[cell.getIndex()];
                        
                        String size = FormatUtil.formatBytesDecimal(disk.getSize());
                        setPopOver(
                                diskPop, 
                                disk.getName(), 
                                "Total: " + size,
                                "Modelo: " + disk.getModel(),
                                "N° de Serie: " + disk.getSerial(),
                                "Escrito: " + FormatUtil.formatBytesDecimal(disk.getWrites()),
                                "Leido: " + FormatUtil.formatBytesDecimal(disk.getReads())
                                );
                        diskPop.show(cell);
                    } else {
                        if (diskPop != null && diskPop.isShowing()) {
                            diskPop.hide();
                        }
                    }
                });
        return cell;                
                
        });
        
        
        vBoxDisks.getChildren().add(listDisks);
        
    }

    private void initCpu() {
//        Util.sleep(1000);

        Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.seconds(2), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                double[] cpuLoad = hardware.getProcessor().getProcessorCpuLoadBetweenTicks();
                for (int i = 0; i < procesadores; i++) {
                    Label l = (Label) cpuToolBar.getChildren().get(i);
                    l.setText(String.format(" %.1f%%", cpuLoad[i] * 100));
                }
            }
        }));
        fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
        fiveSecondsWonder.play();
        
    }
    
    private void setPopOver(PopOver pop, String title, String... messages){
//        pop = new PopOver();
        BorderPane b = new BorderPane();
        b.setPadding(new Insets(10, 20, 10, 20));
        VBox vbox = new VBox();
        Label titleLabel = new Label(title);
        titleLabel.setFont(new Font(18));
        b.setCenter(vbox);
        
        vbox.getChildren().add(titleLabel);
        
        for(String message: messages){
            Label content = new Label(message);
            content.setWrapText(true);
            vbox.getChildren().add(content);
        }
        
        pop.setContentNode(b);
    }
}
