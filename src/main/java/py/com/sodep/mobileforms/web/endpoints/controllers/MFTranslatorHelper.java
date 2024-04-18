package py.com.sodep.mobileforms.web.endpoints.controllers;

import java.util.ArrayList;
import java.util.List;

import py.com.sodep.mf.exchange.objects.metadata.Application;
import py.com.sodep.mf.exchange.objects.metadata.Form;
import py.com.sodep.mf.exchange.objects.metadata.Project;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.projects.ProjectDetails;

/**
 * This class has a collection of static method that translate objects of the
 * domain from entity to mf_captura_exchange
 * 
 * @author danicricco
 * 
 */
class MFTranslatorHelper {

	/**
	 * Translate an entity of type Application to its counterpart in
	 * datomo_exchange
	 * 
	 * @param entity
	 * @return
	 */
	static Application toMf(py.com.sodep.mobileforms.api.entities.application.Application entity) {
		Application app = new Application();
		app.setId(entity.getId());
		app.setLabel(entity.getName());
		return app;
	}

	/***
	 * Translate an entity of type Project to its counterpart in datomo_exchange
	 * 
	 * @param entity
	 * @param details
	 * @return
	 */
	static Project toMf(py.com.sodep.mobileforms.api.entities.projects.Project entity, ProjectDetails details) {
		Project project = new Project();
		project.setId(entity.getId());
		project.setLabel(details.getLabel());
		project.setApplicationId(entity.getApplication().getId());
		project.setDescription(details.getDescription());
		project.setId(entity.getId());
		return project;
	}

	static Form toMf(FormDTO dto) {
		Form form = new Form();
		form.setId(dto.getId());
		form.setLabel(dto.getLabel());
		form.setProjectId(dto.getProjectId());
		form.setVersion(dto.getVersion());
		return form;
	}

	//bug fix for #1737
	static List<Form> toMfWithPublishedVersion(List<FormDTO> dtos){
		ArrayList<Form> list = new ArrayList<Form>();
		for (FormDTO dto : dtos) {
			Form form = toMf(dto);
			list.add(form);
			form.setVersion(dto.getVersionPublished());
		}
		return list;
	}
	
	static List<Form> toMf(List<FormDTO> dtos) {
		ArrayList<Form> list = new ArrayList<Form>();
		for (FormDTO dto : dtos) {
			list.add(toMf(dto));
		}
		return list;
	}

	static Form toMf(py.com.sodep.mobileforms.api.entities.forms.Form entity, String label) {
		Form form = new Form();
		form.setId(entity.getRoot().getId());
		form.setVersion(entity.getVersion().longValue());
		form.setProjectId(entity.getProject().getId());
		form.setLabel(label);
		return form;
	}
}
