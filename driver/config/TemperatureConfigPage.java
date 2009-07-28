package driver.config;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import temperature.TemperatureParams;

public class TemperatureConfigPage implements ConfigPage {

	private JPanel myPanel;

	public TemperatureConfigPage(TemperatureParams params) {
		
		ConfigTableModel ctm = new ConfigTableModel(params, "Temperature Parameters");
		JTable tab = new MixedValueJTable();
		tab.setModel(ctm);
		JScrollPane sp = new JScrollPane(tab);
		sp.setPreferredSize(new Dimension(200, 200));		
		GUI.makeGroupPanel(sp, "Temperature Parameters");
		
		ConfigTableModel agentConf = new ConfigTableModel(params.agentParams, "Agent");
		JTable agentTable = new MixedValueJTable();
		agentTable.setModel(agentConf);
		JScrollPane sp2 = new JScrollPane(agentTable);
		GUI.makeGroupPanel(sp2, "Agent Preferences");
		GUI.colorHeaders(agentTable, true);
		
		myPanel = new JPanel(new BorderLayout());
		myPanel.add(sp, BorderLayout.NORTH);
		myPanel.add(sp2);
		
	}
	
	@Override
	public JPanel getPanel() {
		return myPanel;
	}

	@Override
	public void validateUI() throws IllegalArgumentException {

	}

}