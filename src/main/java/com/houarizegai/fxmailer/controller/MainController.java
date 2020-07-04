package com.houarizegai.fxmailer.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.houarizegai.fxmailer.App;
import com.houarizegai.fxmailer.engine.EmailEngine;
import com.houarizegai.fxmailer.engine.TemplateBuilder;
import com.houarizegai.fxmailer.model.Receiver;
import com.houarizegai.fxmailer.util.GenerateKeys;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

    /***
     * attach file path
     */
    private File attachFile;
    /***
     * attach file name
     */
    @FXML
    private Label lblAttachame;

    private FileChooser imgChooser;

    private FileChooser attachFileChooser;

    private String htmlTemplate;

    /* Start sending status */

    @FXML
    private StackPane stackSendingContainer;

    @FXML
    private Label lblNumberOfSent, lblNumberOfReceivers, lblNumberOfSuccess, lblNumberOfFailed;

    @FXML
    private JFXProgressBar progressSending;

    @FXML
    private JFXButton btnDone;

    /* End sending status */

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // init combobox
        comboRecevicesFormatType.getItems().add("JSON");
        comboRecevicesFormatType.getSelectionModel().selectFirst();

        // init image chooser
        imgChooser = new FileChooser();
        FileChooser.ExtensionFilter imgChooserExtension = new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg", "*.gif");
        imgChooser.getExtensionFilters().add(imgChooserExtension);

        //init file chooser
        attachFileChooser = new FileChooser();
        FileChooser.ExtensionFilter fileChooserExtension = new FileChooser.ExtensionFilter("Attach", "*.txt", "*.zip", "*.tar");
        attachFileChooser.getExtensionFilters().add(fileChooserExtension);
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
        TemplateBuilder templateBuilder = new TemplateBuilder()
                .setHeader("cid:headerImage", fieldHeaderTitle.getText())
                .setFooter(fieldFooterAbout.getText(), areaFooterContact.getText());

        EmailEngine emailEngine = new EmailEngine()
                .setAuth(fieldSenderEmail.getText().trim(), fieldSenderPassword.getText())
                .setSubject(fieldSubject.getText());

        if("JSON".equalsIgnoreCase(comboRecevicesFormatType.getSelectionModel().getSelectedItem())) {
            stackSendingContainer.setVisible(true);

            Gson gson = new Gson();
            List<Receiver> receivers = gson.fromJson(areaTo.getText().trim(), new TypeToken<List<Receiver>>(){}.getType());

            clearSendingStatus();
            int numberOfReceivers = receivers.size();
            lblNumberOfReceivers.setText(String.valueOf(numberOfReceivers));

            new Thread(()->
            Platform.runLater(() -> {
                int numberOfSent = 0;
                for(Receiver receiver : receivers) {
                   templateBuilder.setBody(areaBody.getText()
                            .replaceFirst("<name>", "<span style='color: #2196f3'>" + receiver.getName() + "</span>")
                            .replace("<name>", receiver.getName()));

                    // init email engine
                    emailEngine.setContent(templateBuilder.build())
                            .setHeaderImage(headerImg.getPath())
                            .setAttachFile("KeyPair/publicKey");

                    boolean isSent = emailEngine.send(receiver.getEmail());
                    numberOfSent++;
                    if (isSent) {
                        System.out.println(String.format("%s -> Success [%d/%d]", receiver.getEmail(), numberOfSent, numberOfReceivers));
                        lblNumberOfSuccess.setText(String.valueOf(Integer.parseInt(lblNumberOfSuccess.getText()) + 1));
                    } else {
                        System.out.println(String.format("%s -> Failed [%d/%d]", receiver.getEmail(), numberOfSent, numberOfReceivers));
                        lblNumberOfFailed.setText(String.valueOf(Integer.parseInt(lblNumberOfFailed.getText()) + 1));
                    }

                    lblNumberOfSent.setText(String.valueOf(Integer.valueOf(lblNumberOfSent.getText()) + 1));
                    progressSending.setProgress(Integer.valueOf(lblNumberOfSent.getText()) / (double) numberOfReceivers);

                    btnDone.setDisable(false);
                }
            })
            ).start();
        }

    }

    private void clearSendingStatus() {
        lblNumberOfSent.setText("0");
        lblNumberOfReceivers.setText(null);
        lblNumberOfSuccess.setText("0");
        lblNumberOfFailed.setText("0");
        progressSending.setProgress(0d);
        btnDone.setDisable(true);
    }

    /* sending status actions */

    @FXML
    private void onDone() {
        stackSendingContainer.setVisible(false);
    }

    /***
     * 获得附件地址
     */
    @FXML
    public void onLoadAttach() {
        //generate key
        GenerateKeys gk;
        try {
            gk = new GenerateKeys(1024);
            gk.createKeys();
            gk.writeToFile("KeyPair/publicKey", gk.getPublicKey().getEncoded());
            gk.writeToFile("KeyPair/privateKey", gk.getPrivateKey().getEncoded());
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        lblAttachame.setText("publicKey");
    }
}
