package org.jabref.gui.preferences.externalfiletypes;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;

import org.jabref.gui.DialogService;
import org.jabref.gui.desktop.os.NativeDesktop;
import org.jabref.gui.util.BaseDialog;
import org.jabref.gui.util.FileDialogConfiguration;
import org.jabref.logic.util.OS;

import com.airhacks.afterburner.views.ViewLoader;
import jakarta.inject.Inject;

public class EditExternalFileTypeEntryDialog extends BaseDialog<Void> {

    @FXML private RadioButton defaultApplication;
    @FXML private ToggleGroup applicationToggleGroup;
    @FXML private TextField extension;
    @FXML private TextField name;
    @FXML private TextField mimeType;
    @FXML private RadioButton customApplication;
    @FXML private TextField selectedApplication;
    @FXML private Button btnBrowse;
    @FXML private Label icon;
    @Inject private DialogService dialogService;

    private final NativeDesktop nativeDesktop = OS.getNativeDesktop();
    private final FileDialogConfiguration fileDialogConfiguration = new FileDialogConfiguration.Builder().withInitialDirectory(nativeDesktop.getApplicationDirectory()).build();

    private final ExternalFileTypeItemViewModel item;

    private final boolean isNewItem;
    private EditExternalFileTypeViewModel viewModel;

    public EditExternalFileTypeEntryDialog(ExternalFileTypeItemViewModel item, String dialogTitle) {
        this.isNewItem = (item.extensionProperty().get().equals("")) ? true : false;

        this.item = item;

        this.setTitle(dialogTitle);

        ViewLoader.view(this)
                  .load()
                  .setAsDialogPane(this);

        final Button btOk = (Button) this.getDialogPane().lookupButton(ButtonType.OK);
        btOk.addEventFilter(ActionEvent.ACTION, event -> {
            if (!isValidExternalFileTypeEntry()) {
                event.consume();
            }
        });

        this.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                viewModel.storeSettings();
            } else {
                name.setText("");
            }
            return null;
        });
    }

    public boolean isValidExternalFileTypeEntry() {
        if (name.getText().trim().equalsIgnoreCase("")
                || extension.getText().trim().equalsIgnoreCase("")
                || mimeType.getText().trim().equalsIgnoreCase("")
            ) {
            return false;
        }
        return true;
    }

    @FXML
    public void initialize() {
        viewModel = new EditExternalFileTypeViewModel(item);

        icon.setGraphic(viewModel.getIcon());

        defaultApplication.selectedProperty().bindBidirectional(viewModel.defaultApplicationSelectedProperty());
        customApplication.selectedProperty().bindBidirectional(viewModel.customApplicationSelectedProperty());
        selectedApplication.disableProperty().bind(viewModel.defaultApplicationSelectedProperty());
        btnBrowse.disableProperty().bind(viewModel.defaultApplicationSelectedProperty());

        extension.textProperty().bindBidirectional(viewModel.extensionProperty());
        extension.setEditable(isNewItem);
        name.textProperty().bindBidirectional(viewModel.nameProperty());
        mimeType.textProperty().bindBidirectional(viewModel.mimeTypeProperty());
        selectedApplication.textProperty().bindBidirectional(viewModel.selectedApplicationProperty());
    }

    @FXML
    private void openFileChooser(ActionEvent event) {
        dialogService.showFileOpenDialog(fileDialogConfiguration).ifPresent(path -> viewModel.selectedApplicationProperty().setValue(path.toAbsolutePath().toString()));
    }
}
