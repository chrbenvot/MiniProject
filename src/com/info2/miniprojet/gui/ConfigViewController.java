package com.info2.miniprojet.gui;

import com.info2.miniprojet.MiniProject;
import com.info2.miniprojet.config.Configuration;
import com.info2.miniprojet.factory.StrategyFactory;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigViewController {

    // --- FXML Injected Fields ---
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

    // Preprocessor Configuration UI
    @FXML private ComboBox<String> preprocessorModeComboBox;
    @FXML private HBox pipelineBuilderBox;
    @FXML private ListView<String> availableStagesListView;
    @FXML private ListView<String> selectedStagesListView;
    @FXML private Button addStageButton;
    @FXML private Button removeStageButton;
    @FXML private Button moveStageUpButton;
    @FXML private Button moveStageDownButton;

    private MiniProject backendApp;
    private Configuration currentConfigCopy; // Work on a copy to allow cancellation

    // List of NameComparator types that require an internal StringComparator
    // Ensure these string literals match the keys in StrategyFactory exactly (case-sensitive after toUpperCase)
    private static final List<String> NAME_COMPS_REQUIRING_STRING_COMP = Arrays.asList(
            "PASS_THROUGH_NAME",
            "POSITIONAL_WEIGHTED",
            "BAG_OF_WORDS"
            // Add other NameComparator FACTORY KEYS (UPPERCASE) here if they use a StringComparator
    );

    // ObservableLists for the pipeline ListViews
    private ObservableList<String> availableProcessorStagesObservable = FXCollections.observableArrayList();
    private ObservableList<String> selectedProcessorStagesObservable = FXCollections.observableArrayList();


    public void setBackendApp(MiniProject backendApp) {
        this.backendApp = backendApp;
        if (this.backendApp != null && this.backendApp.getCurrentConfig() != null) {
            this.currentConfigCopy = createConfigCopy(this.backendApp.getCurrentConfig());
            populateControls(); // Load current settings into UI
            initializePipelineControlsState(); // Set up pipeline UI based on loaded config
        } else {
            System.err.println("ConfigViewController Error: BackendApp or its configuration is null during setBackendApp!");
            // Consider disabling the save button or showing an error in the UI
        }
        // These should be called after currentConfigCopy is set
        updateResultModeControls();
        updateStringComparatorVisibility();
    }

    private Configuration createConfigCopy(Configuration original) {
        Configuration copy = new Configuration();
        if (original == null) { // Should ideally not happen if backendApp.init() is robust
            System.err.println("ConfigViewController Error: Original configuration is null in createConfigCopy. Creating default.");
            // Fallback to default if original is somehow null
            original = new Configuration(); // Or load a default from MiniProject
            // Populate 'original' with defaults here if MiniProject.createDefaultConfig() is not accessible
            original.setPreprocessorChoice("NOOP");
            original.setCandidateFinderChoice("CARTESIAN_FIND_ALL");
            original.setNameComparatorChoice("PASS_THROUGH_NAME");
            original.setStringComparatorForNameCompChoice("EXACT_STRING");
            original.setResultThreshold(0.85);
            original.setMaxResults(20);
            original.setThresholdMode(false);
        }

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
        List<String> allPreprocessorChoices = new ArrayList<>(StrategyFactory.getAvailablePreprocessorChoices());
        preprocessorModeComboBox.setItems(FXCollections.observableArrayList(allPreprocessorChoices));

        candidateFinderComboBox.setItems(FXCollections.observableArrayList(StrategyFactory.getAvailableCandidateFinderChoices()));
        nameComparatorComboBox.setItems(FXCollections.observableArrayList(StrategyFactory.getAvailableNameComparatorChoices()));
        stringComparatorComboBox.setItems(FXCollections.observableArrayList(StrategyFactory.getAvailableStringComparatorChoices()));

        // Setup for pipeline ListViews
        List<String> individualStages = StrategyFactory.getAvailablePreprocessorChoices().stream()
                .filter(s -> !s.equalsIgnoreCase("PIPELINE"))
                .collect(Collectors.toList());
        availableProcessorStagesObservable.setAll(individualStages);
        availableStagesListView.setItems(availableProcessorStagesObservable);
        selectedStagesListView.setItems(selectedProcessorStagesObservable);

        // Configure Spinners' value factories if not fully defined or overridden in FXML
        if (thresholdSpinner.getValueFactory() == null) {
            thresholdSpinner.setValueFactory(
                    new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.85, 0.01)
            );
        } else { // Ensure initial value is set from FXML or a default
            thresholdSpinner.getValueFactory().setValue(0.85);
        }
        if (maxResultsSpinner.getValueFactory() == null) {
            maxResultsSpinner.setValueFactory(
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 20, 1)
            );
        } else {
            maxResultsSpinner.getValueFactory().setValue(20);
        }
        System.out.println("GUI: ConfigViewController initialized and ComboBoxes populated.");
    }

    private void populateControls() {
        if (currentConfigCopy == null) {
            System.err.println("ConfigViewController Error: currentConfigCopy is null in populateControls. Cannot set UI values.");
            return;
        }

        // Handle Preprocessor Choice (Single or Pipeline)
        String currentPreprocessorSetting = currentConfigCopy.getPreprocessorChoice();
        if (currentPreprocessorSetting != null && currentPreprocessorSetting.toUpperCase().startsWith("PIPELINE:")) {
            preprocessorModeComboBox.setValue("PIPELINE");
            String stagesStr = currentPreprocessorSetting.substring("PIPELINE:".length());
            if (!stagesStr.isEmpty()) {
                selectedProcessorStagesObservable.setAll(Arrays.asList(stagesStr.split(",")));
            } else {
                selectedProcessorStagesObservable.clear();
            }
        } else if (currentPreprocessorSetting != null) {
            preprocessorModeComboBox.setValue(currentPreprocessorSetting);
            selectedProcessorStagesObservable.clear();
        } else {
            preprocessorModeComboBox.setValue("NOOP"); // Default if null
            selectedProcessorStagesObservable.clear();
        }

        candidateFinderComboBox.setValue(currentConfigCopy.getCandidateFinderChoice());
        nameComparatorComboBox.setValue(currentConfigCopy.getNameComparatorChoice());
        stringComparatorComboBox.setValue(currentConfigCopy.getStringComparatorForNameCompChoice());

        if (currentConfigCopy.isThresholdMode()) {
            thresholdModeRadio.setSelected(true);
        } else {
            maxResultsModeRadio.setSelected(true);
        }
        // Ensure spinner value factories are not null before setting value
        if (thresholdSpinner.getValueFactory() != null) {
            thresholdSpinner.getValueFactory().setValue(currentConfigCopy.getResultThreshold());
        }
        if (maxResultsSpinner.getValueFactory() != null) {
            maxResultsSpinner.getValueFactory().setValue(currentConfigCopy.getMaxResults());
        }
    }

    private void initializePipelineControlsState() {
        // Update visibility based on the initially loaded preprocessorModeComboBox value
        onPreprocessorModeSelected();
    }

    @FXML
    private void onPreprocessorModeSelected() {
        String mode = preprocessorModeComboBox.getValue();
        boolean isPipelineMode = mode != null && mode.equalsIgnoreCase("PIPELINE");
        pipelineBuilderBox.setVisible(isPipelineMode);
        pipelineBuilderBox.setManaged(isPipelineMode);
    }

    @FXML
    private void addStageToPipelineAction() {
        String selected = availableStagesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedProcessorStagesObservable.add(selected);
        }
    }

    @FXML
    private void removeStageFromPipelineAction() {
        String selected = selectedStagesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selectedProcessorStagesObservable.remove(selected);
        }
    }

    @FXML
    private void moveStageUpAction() {
        int selectedIndex = selectedStagesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            String item = selectedProcessorStagesObservable.remove(selectedIndex);
            selectedProcessorStagesObservable.add(selectedIndex - 1, item);
            selectedStagesListView.getSelectionModel().select(selectedIndex - 1);
        }
    }

    @FXML
    private void moveStageDownAction() {
        int selectedIndex = selectedStagesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1 && selectedIndex < selectedProcessorStagesObservable.size() - 1) {
            String item = selectedProcessorStagesObservable.remove(selectedIndex);
            selectedProcessorStagesObservable.add(selectedIndex + 1, item);
            selectedStagesListView.getSelectionModel().select(selectedIndex + 1);
        }
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
        stringComparatorLabel.setManaged(needsStringComp);
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
        thresholdLabel.setManaged(isThreshold);
        thresholdSpinner.setManaged(isThreshold);

        maxResultsLabel.setVisible(!isThreshold);
        maxResultsSpinner.setVisible(!isThreshold);
        maxResultsLabel.setManaged(!isThreshold);
        maxResultsSpinner.setManaged(!isThreshold);
    }

    @FXML
    private void saveConfigurationAction() {
        if (backendApp == null || currentConfigCopy == null) {
            System.err.println("Cannot save: BackendApp or ConfigCopy is null.");
            return;
        }

        // Handle Preprocessor Choice
        String preprocessorMode = preprocessorModeComboBox.getValue();
        if (preprocessorMode != null && preprocessorMode.equalsIgnoreCase("PIPELINE")) {
            if (!selectedProcessorStagesObservable.isEmpty()) {
                String pipelineDefinition = "PIPELINE:" +
                        String.join(",", selectedProcessorStagesObservable);
                backendApp.setPreprocessorChoice(pipelineDefinition);
            } else {
                    backendApp.setPreprocessorChoice("NOOP");
                System.out.println("GUI Config: Pipeline selected but no stages defined, defaulting Preprocessor to NOOP.");
            }
        } else if (preprocessorMode != null) {
            backendApp.setPreprocessorChoice(preprocessorMode);
        } else { // Fallback if nothing selected in mode combobox
            backendApp.setPreprocessorChoice("NOOP");
        }

        // Save other choices
        if (candidateFinderComboBox.getValue() != null) backendApp.setCandidateFinderChoice(candidateFinderComboBox.getValue());
        String selectedNameComp = nameComparatorComboBox.getValue();
        if (selectedNameComp != null) backendApp.setNameComparatorChoice(selectedNameComp);

        if (selectedNameComp != null && NAME_COMPS_REQUIRING_STRING_COMP.contains(selectedNameComp.toUpperCase())) {
            if (stringComparatorComboBox.getValue() != null) {
                backendApp.setStringComparatorForNameCompChoice(stringComparatorComboBox.getValue());
            } else {
                backendApp.setStringComparatorForNameCompChoice("EXACT_STRING");
            }
        } else {
            backendApp.setStringComparatorForNameCompChoice(currentConfigCopy.getStringComparatorForNameCompChoice()); // Keep old if not applicable or not set
        }

        backendApp.setResultMode(thresholdModeRadio.isSelected());
        if (thresholdModeRadio.isSelected()) {
            backendApp.setResultThreshold(thresholdSpinner.getValue());
        } else {
            backendApp.setMaxResults(maxResultsSpinner.getValue());
        }

        System.out.println("GUI: Configuration saved via MiniProject setters.");
        closeWindow();
    }

    @FXML
    private void cancelAction() {
        System.out.println("GUI: Configuration changes cancelled.");
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) (saveButton.getScene() != null ? saveButton.getScene().getWindow() : null);
        if (stage != null) {
            stage.close();
        } else {
            System.err.println("ConfigViewController Error: Could not get stage to close window.");
        }
    }
}