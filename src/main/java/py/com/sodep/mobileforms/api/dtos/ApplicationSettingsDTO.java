package py.com.sodep.mobileforms.api.dtos;

public class ApplicationSettingsDTO {

	private boolean hasWorkflow = false;

	public ApplicationSettingsDTO(boolean hasWorkflow) {
		this.hasWorkflow = hasWorkflow;
	}
	
	public boolean getHasWorkflow() {
		return this.hasWorkflow;
	}
}
