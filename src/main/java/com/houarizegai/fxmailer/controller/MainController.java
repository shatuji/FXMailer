package com.houarizegai.fxmailer.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.houarizegai.fxmailer.App;
import com.houarizegai.fxmailer.engine.EmailEngine;
import com.houarizegai.fxmailer.engine.TemplateBuilder;
import com.houarizegai.fxmailer.model.Receiver;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private JFXTextField fieldSubject, fieldSenderEmail;
    @FXML
    private JFXPasswordField fieldSenderPassword;

    @FXML
    private JFXComboBox<String> comboRecevicesFormatType;
    @FXML
    private JFXTextArea areaTo;

    @FXML
    private JFXTextField fieldHeaderTitle;
    @FXML
    private Label lblHeaderImgName;

    @FXML
    private JFXTextArea areaBody;

    @FXML
    private JFXTextField fieldFooterAbout;
    @FXML
    private JFXTextArea areaFooterContact;

    @FXML
    private WebView webViewTemplate;

    private File headerImg;

    private FileChooser imgChooser;

    private String htmlTemplate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // init combobox
        comboRecevicesFormatType.getItems().add("JSON");
        comboRecevicesFormatType.getSelectionModel().selectFirst();

        // init image chooser
        imgChooser = new FileChooser();
        FileChooser.ExtensionFilter imgChooserExtension = new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg", "*.gif");
        imgChooser.getExtensionFilters().add(imgChooserExtension);
    }

    @FXML
    private void onLoadHeaderImage() {
        headerImg = imgChooser.showOpenDialog(App.stage);
        if(headerImg != null)
            lblHeaderImgName.setText(headerImg.getName());
    }

    @FXML
    private void onPreview() {
        htmlTemplate = new TemplateBuilder()
                .setHeader(headerImg == null ? "" : headerImg.getPath(), fieldHeaderTitle.getText())
                .setBody(areaBody.getText())
                .setFooter(fieldFooterAbout.getText(), areaFooterContact.getText())
                .build();

        webViewTemplate.getEngine().loadContent(htmlTemplate);
    }

    @FXML
    private void onSend() {
        htmlTemplate = new TemplateBuilder()
                .setHeader("cid:headerImage", fieldHeaderTitle.getText())
                .setBody(areaBody.getText())
                .setFooter(fieldFooterAbout.getText(), areaFooterContact.getText())
                .build();

        EmailEngine emailEngine = new EmailEngine()
                .setAuth(fieldSenderEmail.getText().trim(), fieldSenderPassword.getText())
                .setSubject(fieldSubject.getText());

        if("JSON".equalsIgnoreCase(comboRecevicesFormatType.getSelectionModel().getSelectedItem())) {
            Gson gson = new Gson();
            List<Receiver> receivers = gson.fromJson(areaTo.getText().trim(), new TypeToken<List<Receiver>>(){}.getType());

            for(Receiver receiver : receivers) {
                htmlTemplate = htmlTemplate.replaceFirst("<name>", "<span style='color: #2196f3'>" + receiver.getName() + "</span>")
                        .replace("<name>", receiver.getName());

                // init email engine
                emailEngine.setContent(htmlTemplate)
                        .setHeaderImage(headerImg.getPath());

                if(emailEngine.send(receiver.getEmail()))
                    System.out.println(receiver.getEmail() + " -> Success");
            }
            System.out.println("Done!");
        }

    }

}