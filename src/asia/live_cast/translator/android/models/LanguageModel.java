package asia.live_cast.translator.android.models;

public class LanguageModel {
	private String label;
	private String value;
	
	public LanguageModel(String label, String value) {
		this.label = label;
		this.value = value;
	}

	public void setLabel(String _label) {
		this.label = _label;
	}

	public String getLabel() {
		return label;
	}

	public void setValue(String _value) {
		this.value = _value;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return label;
	}
}
