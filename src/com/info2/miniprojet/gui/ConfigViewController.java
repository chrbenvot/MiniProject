package com.info2.miniprojet.gui;

import com.info2.miniprojet.MiniProject;
import com.info2.miniprojet.config.Configuration;
import com.info2.miniprojet.factory.StrategyFactory;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.util.List;
import java.util.Arrays; // For checking NameComparator types

public class ConfigViewController {

    @FXML private ComboBox<String> preprocessorComboBox;
    @FXML private ComboBox<String> candidateFinderComboBox;
    @FXML private ComboBox<String> nameComparatorComboBox;
    @FXML private Label stringComparatorLabel;
    @FXML private ComboBox<String> stringComparatorComboBox;
    @FXML private RadioButton thresholdModeRadio;
    @FXML private RadioButton maxResultsModeRadio;
    @FXML private ToggleGroup resultModeToggleGroup;
    @FXML private Label thresholdLabel;
    @FXML private Spinner<Double> thresholdSpinner;
    @FXML private Label maxResultsLabel;
    @FXML private Spinner<Integer> maxResultsSpinner;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private MiniProject backendApp;
    private Configuration currentConfigCopy; // Work on a copy

    // Helper list from CliHandler (or redefine here, or get from StrategyFactory if it has such a list)
    private static final List<String> NAME_COMPS_REQUIRING_STRING_COMP = Arrays.asList(
            "PASS_THROUGH_NAME",
            "POSITIONAL_WEIGHTED",
            "BAG_OF_WORDS"
            // Add other NameComparator factory keys here if they use a StringComparator
    );

    public void setBackendApp(MiniProject backendApp) {
        this.backendApp = backendApp;
        // Create a copy of the current configuration to work with
        this.currentConfigCopy = createConfigCopy(backendApp.getCurrentConfig());
        populateControls();
        updateResultModeControls(); // Initial setup of visibility
        updateStringComparatorVisibility(); // Initial setup
    }

    private Configuration createConfigCopy(Configuration original) {
        Configuration copy = new Configuration();
        copy.setPreprocessorChoice(original.getPreprocessorChoice());
        copy.setCandidateFinderChoice(original.getCandidateFinderChoice());
        copy.setNameComparatorChoice(original.getNameComparatorChoice());
        copy.setStringComparatorForNameCompChoice(original.getStringComparatorForNameCompChoice());
        copy.setResultThreshold(original.getResultThreshold());
        copy.setMaxResults(original.getMaxResults());
        copy.setThresholdMode(original.isThresholdMode());
        return copy;
    }

    @FXML
    public void initialize() {
        // Populate ComboBoxes with choices from StrategyFactory
        preprocessorComboBox.setItems(FXCollections.observableArrayList(StrategyFactory.getAvailablePreprocessorChoices()));
        candidateFinderComboBox.setItems(FXCollections.observableArrayList(StrategyFactory.getAvailableCandidateFinderChoices()));
        nameComparatorComboBox.setItems(FXCollections.observableArrayList(StrategyFactory.getAvailableNameComparatorChoices()));
        stringComparatorComboBox.setItems(FXCollections.observableArrayList(StrategyFactory.getAvailableStringComparatorChoices()));

        // Configure Spinners (ranges are set in FXML, could be done here too)
        // SpinnerValueFactory.DoubleSpinnerValueFactory thresholdFactory =
        //         new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.85, 0.01);
        // thresholdSpinner.setValueFactory(thresholdFactory);
        // SpinnerValueFactory.IntegerSpinnerValueFactory maxResultsFactory =
        //         new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 20);
        // maxResultsSpinner.setValueFactory(maxResultsFactory);
    }

    private void populateControls() {
        if (currentConfigCopy == null) return;

        preprocessorComboBox.setValue(currentConfigCopy.getPreprocessorChoice());
        candidateFinderComboBox.setValue(currentConfigCopy.getCandidateFinderChoice());
        nameComparatorComboBox.setValue(currentConfigCopy.getNameComparatorChoice());
        stringComparatorComboBox.setValue(currentConfigCopy.getStringComparatorForNameCompChoice());

        if (currentConfigCopy.isThresholdMode()) {
            thresholdModeRadio.setSelected(true);
        } else {
            maxResultsModeRadio.setSelected(true);
        }
        thresholdSpinner.getValueFactory().setValue(currentConfigCopy.getResultThreshold());
        maxResultsSpinner.getValueFactory().setValue(currentConfigCopy.getMaxResults());
    }

    @FXML
    private void onNameComparatorSelected() {
        updateStringComparatorVisibility();
    }

    private void updateStringComparatorVisibility() {
        String selectedNameComp = nameComparatorComboBox.getValue();
        boolean needsStringComp = selectedNameComp != null &&
                NAME_COMPS_REQUIRING_STRING_COMP.contains(selectedNameComp.toUpperCase());
        stringComparatorLabel.setVisible(needsStringComp);
        stringComparatorComboBox.setVisible(needsStringComp);
        stringComparatorLabel.setManaged(needsStringComp); // To collapse space when not visible
        stringComparatorComboBox.setManaged(needsStringComp);
    }


    @FXML
    private void onResultModeChanged() {
        updateResultModeControls();
    }

    private void updateResultModeControls() {
        boolean isThreshold = thresholdModeRadio.isSelected();
        thresholdLabel.setVisible(isThreshold);
        thresholdSpinner.setVisible(isThreshold);
        thresholdLabel.setManaged(isThreshold); // To collapse space
        thresholdSpinner.setManaged(isThreshold);

        maxResultsLabel.setVisible(!isThreshold);
        maxResultsSpinner.setVisible(!isThreshold);
        maxResultsLabel.setManaged(!isThreshold);
        maxResultsSpinner.setManaged(!isThreshold);
    }

    @FXML
    private void saveConfigurationAction() {
        if (backendApp == null || currentConfigCopy == null) return;

        // Update the original configuration in MiniProject (backendApp)
        backendApp.setPreprocessorChoice(preprocessorComboBox.getValue());
        backendApp.setCandidateFinderChoice(candidateFinderComboBox.getValue());
        backendApp.setNameComparatorChoice(nameComparatorComboBox.getValue());

        String selectedNameComp = nameComparatorComboBox.getValue();
        if (selectedNameComp != null && NAME_COMPS_REQUIRING_STRING_COMP.contains(selectedNameComp.toUpperCase())) {
            backendApp.setStringComparatorForNameCompChoice(stringComparatorComboBox.getValue());
        } else {
            // If selected NameComparator doesn't use StringComparator,
            // we might set the config string to null or a default "N/A"
            // or keep the last value. For simplicity, let's keep it if not applicable.
            // Or, ensure a default is always set:
            if (stringComparatorComboBox.getValue() != null) {
                backendApp.setStringComparatorForNameCompChoice(stringComparatorComboBox.getValue());
            } else {
                backendApp.setStringComparatorForNameCompChoice("EXACT_STRING"); // Default
            }
        }


        backendApp.setResultMode(thresholdModeRadio.isSelected());
        if (thresholdModeRadio.isSelected()) {
            backendApp.setResultThreshold(thresholdSpinner.getValue());
        } else {
            backendApp.setMaxResults(maxResultsSpinner.getValue());
        }

        System.out.println("GUI: Configuration saved.");
        closeWindow();
    }

    @FXML
    private void cancelAction() {
        System.out.println("GUI: Configuration cancelled.");
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}