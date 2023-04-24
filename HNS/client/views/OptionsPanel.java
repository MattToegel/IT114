package HNS.client.views;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;

import HNS.common.GameOptions;

public class OptionsPanel extends JPanel {
    private static Logger logger = Logger.getLogger(OptionsPanel.class.getName());
    private GameOptions options = new GameOptions();
    private JLabel seeksLabel = new JLabel("Seeker Guesses per round:");
    private JSlider seeksSlider = new JSlider(1, 10, options.getSeeksPerRound());
    private JLabel eliminationModeLabel = new JLabel("Toggle Elimination Mode:");
    private JCheckBox eliminationModeCB = new JCheckBox("Enabled", options.isEliminationMode());
    private JLabel blockageLabel = new JLabel("Percent Blockage:");
    private JSlider blockageSlider = new JSlider(10, 95, options.getBlockage());
    private Consumer<GameOptions> callback = null;

    private int lastSeeks;
    private boolean lastElim;
    private int lastBlockage;

    public OptionsPanel(boolean isHost) {

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(seeksLabel);
        this.add(seeksSlider);
        this.add(eliminationModeLabel);
        this.add(eliminationModeCB);
        this.add(blockageLabel);
        this.add(blockageSlider);
        seeksSlider.setEnabled(isHost);
        seeksSlider.setPaintTrack(true);
        seeksSlider.setPaintTicks(true);
        seeksSlider.setPaintLabels(true);
        seeksSlider.setMajorTickSpacing(1);
        eliminationModeCB.setEnabled(isHost);
        blockageSlider.setEnabled(isHost);
        blockageSlider.setPaintTrack(true);
        blockageSlider.setPaintTicks(true);
        blockageSlider.setPaintLabels(true);
        blockageSlider.setMinorTickSpacing(5);
        blockageSlider.setMajorTickSpacing(10);
        removeListeners();
        attachListeners();
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                logger.info("Attaching listeners");
                attachListeners();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentShown(e);
                removeListeners();
                logger.info("Removing listeners");
                callback = null;
            }
        });
        this.revalidate();
        this.repaint();
    }

    public void setIsHost(boolean isHost) {
        seeksSlider.setEnabled(isHost);
        eliminationModeCB.setEnabled(isHost);
        blockageSlider.setEnabled(isHost);
    }

    public void setCallback(Consumer<GameOptions> callback) {
        this.callback = callback;
    }

    private void attachListeners() {
        seeksSlider.addChangeListener(l -> {

            options.setSeeksPerRound(seeksSlider.getValue());
            if (lastSeeks != options.getSeeksPerRound()) {
                lastSeeks = options.getSeeksPerRound();
                dataChanged();
            }

        });
        eliminationModeCB.addChangeListener(l -> {
            options.setEliminationMode(eliminationModeCB.isSelected());
            if (lastElim != options.isEliminationMode()) {
                lastElim = options.isEliminationMode();
                dataChanged();
            }
        });
        blockageSlider.addChangeListener(l -> {

            options.setBlockage(blockageSlider.getValue());
            if (lastBlockage != options.getBlockage()) {
                lastBlockage = options.getBlockage();
                dataChanged();
            }

        });
    }

    private void dataChanged() {
        logger.info("Data changed");
        if (callback != null) {
            callback.accept(options);
        }
    }

    private void removeListeners() {
        for (ChangeListener cl : seeksSlider.getChangeListeners()) {
            seeksSlider.removeChangeListener(cl);
        }
        for (ChangeListener cl : eliminationModeCB.getChangeListeners()) {
            eliminationModeCB.removeChangeListener(cl);
        }
        for (ChangeListener cl : blockageSlider.getChangeListeners()) {
            blockageSlider.removeChangeListener(cl);
        }
    }

    public void setOptions(GameOptions go) {
        options = go;
        seeksSlider.setValue(options.getSeeksPerRound());
        eliminationModeCB.setSelected(options.isEliminationMode());
        blockageSlider.setValue(options.getBlockage());
    }

    public GameOptions getOptions() {
        return options;
    }
}
