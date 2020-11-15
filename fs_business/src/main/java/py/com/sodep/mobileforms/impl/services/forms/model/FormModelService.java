package py.com.sodep.mobileforms.impl.services.forms.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.sodep.mf.exchange.MFField;
import py.com.sodep.mf.exchange.MFLoookupTableDefinition;
import py.com.sodep.mf.exchange.MFManagedData;
import py.com.sodep.mf.exchange.objects.lookup.LookupTableDTO;
import py.com.sodep.mf.form.model.MFForm;
import py.com.sodep.mf.form.model.MFPage;
import py.com.sodep.mf.form.model.PropertyMetadata;
import py.com.sodep.mf.form.model.PropertyMetadata.PropertyOption;
import py.com.sodep.mf.form.model.element.MFElement;
import py.com.sodep.mf.form.model.element.filter.MFFilter;
import py.com.sodep.mf.form.model.flow.MFConditionalTarget;
import py.com.sodep.mf.form.model.flow.MFFlow;
import py.com.sodep.mf.form.model.prototype.MFBarcode;
import py.com.sodep.mf.form.model.prototype.MFCheckbox;
import py.com.sodep.mf.form.model.prototype.MFHeadline;
import py.com.sodep.mf.form.model.prototype.MFInput;
import py.com.sodep.mf.form.model.prototype.MFLocation;
import py.com.sodep.mf.form.model.prototype.MFPhoto;
import py.com.sodep.mf.form.model.prototype.MFPrototype;
import py.com.sodep.mf.form.model.prototype.MFSelect;
import py.com.sodep.mf.form.model.prototype.MFSignature;
import py.com.sodep.mf.form.model.prototype.MFSelect.OptionSource;
import py.com.sodep.mobileforms.api.constants.AuthorizationNames;
import py.com.sodep.mobileforms.api.dtos.FormDTO;
import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.dynamicvalues.Filter;
import py.com.sodep.mobileforms.api.entities.forms.Form;
import py.com.sodep.mobileforms.api.entities.forms.elements.Barcode;
import py.com.sodep.mobileforms.api.entities.forms.elements.Checkbox;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementInstance;
import py.com.sodep.mobileforms.api.entities.forms.elements.ElementPrototype;
import py.com.sodep.mobileforms.api.entities.forms.elements.Headline;
import py.com.sodep.mobileforms.api.entities.forms.elements.Input;
import py.com.sodep.mobileforms.api.entities.forms.elements.Location;
import py.com.sodep.mobileforms.api.entities.forms.elements.Photo;
import py.com.sodep.mobileforms.api.entities.forms.elements.Select;
import py.com.sodep.mobileforms.api.entities.forms.elements.Signature;
import py.com.sodep.mobileforms.api.entities.forms.page.ConditionalTarget;
import py.com.sodep.mobileforms.api.entities.forms.page.Flow;
import py.com.sodep.mobileforms.api.entities.forms.page.Page;
import py.com.sodep.mobileforms.api.services.auth.Authorizable;
import py.com.sodep.mobileforms.api.services.data.ILookupTableService;
import py.com.sodep.mobileforms.api.services.forms.model.ElementPrototypeUtils;
import py.com.sodep.mobileforms.api.services.forms.model.IFormModelService;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;
import py.com.sodep.mobileforms.api.services.metadata.forms.IFormService;

@Service("FormModelService")
@Transactional
class FormModelService implements IFormModelService {

	@PersistenceContext(unitName = "mobileforms")
	private EntityManager em;

	@Autowired
	private IFormService formService;

	@Autowired
	private I18nBundle i18n;

	@Autowired
	private ILookupTableService lookupTableService;

	private interface Condition<T> {
		boolean applies(T param);
	}

	private static <T> Collection<T> findAll(final Collection<T> coll, final Condition<T> condition) {
		List<T> l = new ArrayList<T>();
		if (coll != null) {
			for (T obj : coll) {
				if (condition.applies(obj)) {
					l.add(obj);
				}
			}
		}
		return Collections.unmodifiableList(l);
	}

	private final Condition<ElementInstance> elementNotDeletedCondition = new Condition<ElementInstance>() {
		public boolean applies(final ElementInstance element) {
			return !element.getDeleted();
		}
	};

	private final Condition<Page> pageNotDeletedCondition = new Condition<Page>() {
		public boolean applies(final Page page) {
			return !page.getDeleted();
		}
	};

	@Override
	public MFForm getMFForm(final Long formId, final Long version, final String language) {
		return getMFForm(formId, version, language, true);
	}

	@Override
	public MFForm getMFForm(Form form, String language, final boolean includeMetadata) {
		if (!em.contains(form)) {
			form = em.find(Form.class, form.getId());
		}
		if (language == null) {
			language = form.getRoot().getDefaultLanguage();
		}
		Application app = form.getProject().getApplication();

		MFForm mfform = buildMFForm(form, language, includeMetadata);

		// This is data that's needed in the editor which I don't think that
		// should be part
		// of the form definition - jmpr - 02/01/13
		FormDTO formDTO = formService.getFormDTO(form, language);
		mfform.setPublished(formDTO.isPublished());
		mfform.setVersionPublished(formDTO.getVersionPublished());

		Collection<Page> pages = findAll(form.getPages(), pageNotDeletedCondition);

		for (Page page : pages) {
			MFPage mfpage = buildMFPage(pages, page, language, includeMetadata);
			mfform.getPages().add(mfpage);

			Collection<ElementInstance> elements = findAll(page.getElements(), elementNotDeletedCondition);
			for (ElementInstance element : elements) {
				MFElement mffelement = buildMFElement(app, element, language, includeMetadata);
				mfpage.getElements().add(mffelement);
			}
		}

		return mfform;
	}

	@Override
	@Authorizable(value = AuthorizationNames.Form.READ_WEB, formParam = 0)
	public MFForm getMFForm(final Long formId, final Long version, final String language, final boolean includeMetadata) {
		Form form = formService.getForm(formId, version);
		return getMFForm(form, language, includeMetadata);
	}

	private MFForm buildMFForm(final Form form, final String language, final boolean includeMetadata) {
		String lang = language;
		if (lang == null) {
			lang = form.getDefaultLanguage();
		}
		MFForm mfform = new MFForm();
		mfform.setId(form.getRoot().getId());
		mfform.setPages(new ArrayList<MFPage>());
		mfform.setLabel(form.getLabel(lang));
		mfform.setVersion(form.getVersion());
		mfform.setProvideLocation(form.getProvideLocation() != null && form.getProvideLocation());
		mfform.setWasPublished(form.getWasPublished());
		// metadata
		// TODO Consider adding a parameter to avoid creating this. (The device
		// or the input page won't use it)
		if (includeMetadata) {
			Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();
			mfform.setPropertyMetadata(metadata);
			PropertyMetadata labelMetadata = new PropertyMetadata(i18n.getLabel(lang, "web.editor.properties.label"),
					true, false);
			metadata.put("label", labelMetadata);
			PropertyMetadata provideLocationMetadata = new PropertyMetadata(i18n.getLabel(lang,
					"web.editor.form.properties.provideLocation"), true, true, PropertyMetadata.Type.BOOLEAN);
			metadata.put("provideLocation", provideLocationMetadata);
		}
		return mfform;
	}

	private MFPage buildMFPage(final Collection<Page> pages, final Page page, final String language,
			final boolean includeMetadata) {
		MFPage mfpage = new MFPage();
		mfpage.setId(page.getId());
		mfpage.setPosition(page.getPosition());
		mfpage.setLabel(page.getLabel(language));
		mfpage.setElements(new ArrayList<MFElement>());
		mfpage.setSaveable(page.getSave());
		mfpage.setInstanceId(page.getInstanceId());
		if (!page.getSave()) {
			MFFlow mfflow = buildMFFlow(pages, page);
			mfpage.setFlow(mfflow);
		}
		// metadata
		// TODO Consider adding a parameter to avoid creating this. (The device
		// or the input page won't use it)
		if (includeMetadata) {
			Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();
			mfpage.setPropertyMetadata(metadata);
			PropertyMetadata labelMetadata = new PropertyMetadata(
					i18n.getLabel(language, "web.editor.properties.label"), true, true);
			metadata.put("label", labelMetadata);

			PropertyMetadata saveableMetadata = new PropertyMetadata(i18n.getLabel(language,
					"web.editor.properties.saveable"), true, true, PropertyMetadata.Type.BOOLEAN);
			metadata.put("saveable", saveableMetadata);
		}
		return mfpage;
	}

	private MFFlow buildMFFlow(Collection<Page> pages, Page page) {
		Flow flow = page.getFlow();
		if (flow == null) {
			return null;
		}

		MFFlow mfflow = new MFFlow();
		if (flow.getDefaultTarget() != null) {
			mfflow.setDefaultTarget(flow.getDefaultTarget());
		}

		List<ConditionalTarget> targets = flow.getTargets();
		if (targets != null) {
			List<MFConditionalTarget> mftargets = new ArrayList<MFConditionalTarget>();
			mfflow.setTargets(mftargets);
			int i = 0;
			for (ConditionalTarget target : targets) {
				MFConditionalTarget mft = new MFConditionalTarget();
				mft.setOperator(target.getOperator());
				mft.setElementId(target.getElementId());
				mft.setPreAction(target.getPreAction());
				mft.setTarget(target.getTarget());
				mft.setValue(target.getValue());
				mft.setIndex(i);
				mftargets.add(mft);
				i++;
			}
		}
		return mfflow;
	}

	// private Page getNextPage(Collection<Page> pages, Page page) {
	// int position = page.getPosition();
	// int next = position + 1;
	// for (Page p : pages) {
	// if (p.getPosition() == next) {
	// return p;
	// }
	// }
	// return null;
	// }

	private MFElement buildMFElement(final Application app, final ElementInstance element, final String language,
			final boolean includeMetadata) {
		MFElement mfelement = new MFElement();
		long eid = element.getId();
		mfelement.setId(eid);
		mfelement.setPosition(element.getPosition());

		ElementPrototype prototype = element.getPrototype();

		// FIXME This is very confusing!!!
		boolean embedded = prototype.isEmbedded();
		boolean required = (prototype.getRequired() != null & prototype.getRequired());
		if (embedded) {
			required = required || (element.getRequired() != null && element.getRequired());
		}
		mfelement.setRequired(required);

		mfelement.setDefaultValueLookupTableId(element.getDefaultValueLookupTableId());
		mfelement.setDefaultValueColumn(element.getDefaultValueColumn());
		List<MFFilter> defaultValueFilters = getFilters(element, MFFilter.Type.DEFAULT_VALUE);
		mfelement.setDefaultValueFilters(defaultValueFilters);

		// This has sense for Elements which prototype is of type Select (or
		// autocomplete in a future)
		List<MFFilter> itemListFilters = getFilters(element, MFFilter.Type.ITEM_LIST);
		mfelement.setItemListFilters(itemListFilters);

		mfelement.setInstanceId(element.getInstanceId());

		MFPrototype mfPrototype = buildMFPrototype(app, prototype, language, includeMetadata);
		mfelement.setProto(mfPrototype);

		if (includeMetadata && !(prototype instanceof Headline)) {
			// FIXME This is very confusing!!!
			// remove required fromo prototype
			Map<String, PropertyMetadata> protoMetadata = mfPrototype.getPropertyMetadata();
			protoMetadata.remove("required");

			Map<String, PropertyMetadata> metadata = new HashMap<String, PropertyMetadata>();
			mfelement.setPropertyMetadata(metadata);

			PropertyMetadata requiredMetadata = new PropertyMetadata(i18n.getLabel(language,
					"web.editor.properties.required"), true, embedded, PropertyMetadata.Type.BOOLEAN);
			metadata.put("required", requiredMetadata);

		} else if (includeMetadata && (prototype instanceof Headline)) { // FIXME
																			// !!!
			Map<String, PropertyMetadata> protoMetadata = mfPrototype.getPropertyMetadata();
			protoMetadata.remove("required");
		}

		return mfelement;
	}

	private List<MFFilter> getFilters(final ElementInstance element, final MFFilter.Type type) {
		List<MFFilter> filters = new ArrayList<MFFilter>();
		int i = 0;

		Collection<Filter> elementFilters = findAll(element.getFilters(), new Condition<Filter>() {
			public boolean applies(Filter f) {
				return f.getType() == type;
			}

		});

		for (Filter filter : elementFilters) {
			MFFilter mffilter = new MFFilter();
			mffilter.setIndex(i);
			mffilter.setRightValue(filter.getRightValue());
			mffilter.setType(type);
			mffilter.setOperator(filter.getOperator());
			mffilter.setColumn(filter.getColumn());
			filters.add(mffilter);
			i++;
		}
		;
		return filters;
	}

	@Override
	@Authorizable(checkType = Authorizable.CHECK_TYPE.NONE)
	public MFPrototype buildMFPrototype(final Application app, final ElementPrototype proto, final String language) {
		return buildMFPrototype(app, proto, language, true);
	}

	private MFPrototype buildMFPrototype(final Application app, final ElementPrototype prototype, String language,
			boolean includeMetadata) {
		final ElementPrototype proto;
		if (!em.contains(prototype)) {
			proto = em.find(ElementPrototype.class, prototype.getId());
		} else {
			proto = prototype;
		}

		boolean editable = proto.isEmbedded() || proto.isTemplate();
		//  If a new item is added to the toolbox, here is where a buildMFxxxx should go ------//
		MFPrototype mfproto = null;
		if (proto instanceof Input) {
			mfproto = buildMFInput((Input) proto, editable, language, includeMetadata);
		} else if (proto instanceof Select) {
			mfproto = buildMFSelect(app, (Select) proto, editable, language, includeMetadata);
		} else if (proto instanceof Location) {
			mfproto = buildMFLocation((Location) proto, editable, language, includeMetadata);
		} else if (proto instanceof Photo) {
			mfproto = buildMFPhoto((Photo) proto, editable, language, includeMetadata);
		} else if (proto instanceof Headline) {
			mfproto = buildMFHeadline((Headline) proto, editable, language, includeMetadata);
		} else if (proto instanceof Checkbox) {
			mfproto = buildMFCheckbox((Checkbox) proto, editable, language, includeMetadata);
		} else if(proto instanceof Barcode) {
			mfproto = buildMFBarcode((Barcode) proto, editable, language, includeMetadata);
		} else if(proto instanceof Signature){ 
			mfproto = buildeMFSignature((Signature) proto, editable, language, includeMetadata);
		} else {
			throw new RuntimeException("Invalid prototype");
		}

		boolean template = proto.isTemplate();
		mfproto.setEmbedded(proto.isEmbedded());
		mfproto.setTemplate(template);
		mfproto.setId(proto.getId());
		mfproto.setLabel(proto.getLabel(language));
		mfproto.setName(ElementPrototypeUtils.getName(proto));
		mfproto.setRequired(proto.getRequired() != null && proto.getRequired());

		if (includeMetadata) {
			Map<String, PropertyMetadata> metadata = mfproto.getPropertyMetadata();
			PropertyMetadata labelMetadata = new PropertyMetadata(
					i18n.getLabel(language, "web.editor.properties.label"), true, editable,
					PropertyMetadata.Type.STRING, 10);
			metadata.put("label", labelMetadata);
			// FIXME This is very confusing!!!
			if (!template) {
				PropertyMetadata requiredMetadata = new PropertyMetadata(i18n.getLabel(language,
						"web.editor.properties.required"), true, false, PropertyMetadata.Type.BOOLEAN);
				metadata.put("required", requiredMetadata);
			}
		}

		return mfproto;
	}

	private MFSignature buildeMFSignature(Signature proto, boolean editable, String language, boolean includeMetadata) {
		MFSignature mfSignature = new MFSignature();
		mfSignature.setFile(true); 
		if(includeMetadata){
			Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();
			mfSignature.setPropertyMetadata(metadata);
		}
		return mfSignature;
	}

	private MFPrototype buildMFBarcode(Barcode proto, boolean editable, String language, boolean includeMetadata) {
		MFBarcode mfBarcode = new MFBarcode();
		
		if(includeMetadata){
			Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();
			mfBarcode.setPropertyMetadata(metadata);
		}
		return mfBarcode;
	}

	private MFPrototype buildMFCheckbox(Checkbox proto, boolean editable, String language, boolean includeMetadata) {
		MFCheckbox mfCheckbox = new MFCheckbox();
		mfCheckbox.setChecked(proto.isChecked());
		if(includeMetadata){
			Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();
			mfCheckbox.setPropertyMetadata(metadata);
			// whether it's checked by default
			PropertyMetadata selectedMetadata = new PropertyMetadata(i18n.getLabel(language,
					"web.editor.properties.checkbox.checked"), true, editable, PropertyMetadata.Type.BOOLEAN);
			metadata.put("checked", selectedMetadata);
		}
		return mfCheckbox;
	}

	private MFPrototype buildMFHeadline(final Headline proto, final boolean editable, final String language,
			final boolean includeMetadata) {
		MFHeadline mfheadline = new MFHeadline();

		if (includeMetadata) {
			Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();
			mfheadline.setPropertyMetadata(metadata);
			// PropertyMetadata valueMetadata = new PropertyMetadata(
			// i18n.getLabel(language, "web.editor.properties.value"), true,
			// editable,
			// PropertyMetadata.Type.STRING, 10);
			// metadata.put("value", valueMetadata);
		}
		return mfheadline;
	}

	private MFPhoto buildMFPhoto(final Photo photo, final boolean editable, final String language,
			boolean includeMetadata) {
		MFPhoto mfphoto = new MFPhoto();
		mfphoto.setFile(true); 
		mfphoto.setCameraOnly(photo.isCameraOnly());
		if (includeMetadata) {
			Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();
			mfphoto.setPropertyMetadata(metadata);
			PropertyMetadata cameraOnlyMetadata = new PropertyMetadata(i18n.getLabel(language,
					"web.editor.properties.photo.cameraOnly"), true, editable, PropertyMetadata.Type.BOOLEAN);
			metadata.put("cameraOnly", cameraOnlyMetadata);
		}
		return mfphoto;
	}

	private MFLocation buildMFLocation(final Location location, final boolean editable, String language,
			boolean includeMetadata) {
		MFLocation mflocation = new MFLocation();

		if (location.getDefaultLatitude() != null) {
			mflocation.setDefaultLatitude(location.getDefaultLatitude());
		}

		if (location.getDefaultLongitude() != null) {
			mflocation.setDefaultLongitude(location.getDefaultLongitude());
		}

		if (includeMetadata) {
			Map<String, PropertyMetadata> metadata = mfLocationMetadata(editable, language);
			mflocation.setPropertyMetadata(metadata);
		}
		return mflocation;
	}

	private Map<String, PropertyMetadata> mfLocationMetadata(final boolean editable, final String language) {
		Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();

		// What's the purpose of a default location?
		PropertyMetadata defaultLatitudeMetadata = new PropertyMetadata(i18n.getLabel(language,
				"web.editor.properties.defaultLatitude"), false, editable, PropertyMetadata.Type.DOUBLE);
		metadata.put("defaultLatitude", defaultLatitudeMetadata);

		PropertyMetadata defaultLongitudeMetadata = new PropertyMetadata(i18n.getLabel(language,
				"web.editor.properties.defaultLongitude"), false, editable, PropertyMetadata.Type.DOUBLE);
		metadata.put("defaultLongitude", defaultLongitudeMetadata);
		return metadata;
	}

	private MFSelect buildMFSelect(final Application app, final Select select, final boolean editable,
			final String language, final boolean includeMetadata) {
		MFSelect mfselect = new MFSelect();

		mfselect.setDefaultValue(select.getDefaultValue());
		OptionSource source = select.getSource();
		mfselect.setSource(source);
		mfselect.setLookupTableId(select.getLookupTableId());
		mfselect.setLookupLabel(select.getLookupLabel());
		mfselect.setLookupValue(select.getLookupValue());

		// metadata
		// This is only needed in the designer... Actually, everything that comes
		// after this is only useful for the designer

		if (includeMetadata) {
			if (source == OptionSource.EMBEDDED) {
				List<MFManagedData> optionList = Collections.emptyList();
				if (select.getLookupTableId() != null) {
					optionList = lookupTableService.listAllData(app, select.getLookupTableId());
				}
				List<PropertyOption> optionDTOList = new ArrayList<PropertyOption>();

				for (MFManagedData d : optionList) {
					Object label = d.getValue(select.getLookupLabel());
					Object value = d.getValue(select.getLookupValue());
					// FIXME Should the app force LookupLabel to be String?
					optionDTOList.add(new PropertyOption(label.toString(), value));
				}
				mfselect.setEmbeddedValues(embeddedValuesAsStr(optionDTOList));
			}

			Map<String, PropertyMetadata> metadata = mfSelectMetadata(app, select, editable, language, source);
			mfselect.setPropertyMetadata(metadata);
		}
		return mfselect;
	}

	private Map<String, PropertyMetadata> mfSelectMetadata(final Application app, final Select select,
			final boolean editable, final String language, final OptionSource source) {
		Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();

		PropertyMetadata defaultValueMetadata = new PropertyMetadata(i18n.getLabel(language,
				"web.editor.properties.defaultValue"), true, editable, PropertyMetadata.Type.STRING, 5);
		metadata.put("defaultValue", defaultValueMetadata);

		PropertyMetadata sourceMetadata = new PropertyMetadata(i18n.getLabel(language,
				"web.editor.properties.select.source"), true, editable, PropertyMetadata.Type.ENUM, 9);
		sourceMetadata.setOptions(listSelectSourceOptions(language));
		metadata.put("source", sourceMetadata);

		List<PropertyOption> lookupTables = listLookupTables(app, language);
		PropertyMetadata lookupTableIdMetadata = new PropertyMetadata(i18n.getLabel(language,
				"web.editor.properties.select.lookuptable"), source == OptionSource.LOOKUP_TABLE, editable,
				PropertyMetadata.Type.ENUM, 8);
		lookupTableIdMetadata.setOptions(lookupTables);
		metadata.put("lookupTableId", lookupTableIdMetadata);

		List<PropertyOption> columns = null;
		if (OptionSource.LOOKUP_TABLE == source && select.getLookupTableId() != null) {
			columns = listLookupTableColumns(select.getLookupTableId());
		}

		PropertyMetadata lookupValueMetadata = new PropertyMetadata(i18n.getLabel(language,
				"web.editor.properties.select.lookuptable.value"), source == OptionSource.LOOKUP_TABLE, editable,
				PropertyMetadata.Type.ENUM, 6);
		lookupValueMetadata.setOptions(columns);
		metadata.put("lookupValue", lookupValueMetadata);

		PropertyMetadata lookupLabelMetadata = new PropertyMetadata(i18n.getLabel(language,
				"web.editor.properties.select.lookuptable.label"), source == OptionSource.LOOKUP_TABLE, editable,
				PropertyMetadata.Type.ENUM, 7);
		lookupLabelMetadata.setOptions(columns);
		metadata.put("lookupLabel", lookupLabelMetadata);

		PropertyMetadata embeddedValuesMetadata = new PropertyMetadata(i18n.getLabel(language,
				"web.editor.properties.select.embedded.values"), source == OptionSource.EMBEDDED, editable,
				PropertyMetadata.Type.STRING_LONG, 8);
		metadata.put("embeddedValues", embeddedValuesMetadata);
		return metadata;
	}

	private String embeddedValuesAsStr(final List<PropertyOption> options) {
		StringBuilder sb = new StringBuilder();
		for (PropertyOption o : options) {
			sb.append(o.getLabel() + "\n");
		}
		return sb.toString();
	}

	private List<PropertyOption> listSelectSourceOptions(String language) {
		Collection<OptionSource> sources = MFSelect.getSources();
		List<PropertyOption> options = new ArrayList<PropertyOption>();
		for (OptionSource s : sources) {
			options.add(new PropertyOption(
					i18n.getLabel(language, "web.editor.properties.select.source." + s.getName()), s));
		}
		return options;
	}

	private List<PropertyOption> listLookupTables(final Application app, final String language) {
		List<LookupTableDTO> lookupTables = lookupTableService.listAvailableLookupTables(app);

		List<PropertyOption> options = new ArrayList<PropertyOption>();
		options.add(new PropertyOption(i18n.getLabel(language, "web.data.select.lookup_table"), -1));
		for (LookupTableDTO l : lookupTables) {
			options.add(new PropertyOption(l.getName(), l.getPk()));
		}
		return options;
	}

	private List<PropertyOption> listLookupTableColumns(final Long lutId) {
		MFLoookupTableDefinition lookupTableDefinition = lookupTableService.getLookupTableDefinition(lutId);
		List<PropertyOption> options = new ArrayList<PropertyOption>();
		List<MFField> fields = lookupTableDefinition.getFields();
		for (MFField f : fields) {
			options.add(new PropertyOption(f.getColumnName(), f.getColumnName()));
		}

		return options;
	}

	private MFInput buildMFInput(final Input input, final boolean editable, final String language,
			final boolean includeMetadata) {
		MFInput mfinput = new MFInput();
		//the input.type is what is known as the MFInput.subType
		MFInput.Type inputType = input.getType();
		
		mfinput.setSubtype(inputType);
		mfinput.setDefaultValue(input.getDefaultValue()); // TODO translate
															// default value?
		mfinput.setReadOnly(input.getReadOnly());
		mfinput.setMinLength(input.getMinLength());
		mfinput.setMaxLength(input.getMaxLength());

		// metadata
		PropertyMetadata.Type t = PropertyMetadata.Type.STRING;
		if (inputType == MFInput.Type.DATE) {
			t = PropertyMetadata.Type.DATE;
		} else if (inputType == MFInput.Type.TIME) {
			t = PropertyMetadata.Type.TIME;
		}else if (inputType == MFInput.Type.INTEGER) {
			t = PropertyMetadata.Type.INTEGER;
		}else if (inputType == MFInput.Type.DECIMAL) {
			t = PropertyMetadata.Type.DOUBLE;
		}

		if (includeMetadata) {
			Map<String, PropertyMetadata> metadata = mfInputMetadata(editable, language, inputType, t);
			mfinput.setPropertyMetadata(metadata);
		}
		return mfinput;
	}

	private Map<String, PropertyMetadata> mfInputMetadata(final boolean editable, final String language,
			final MFInput.Type inputType, final PropertyMetadata.Type t) {
		Map<String, PropertyMetadata> metadata = new LinkedHashMap<String, PropertyMetadata>();

		if (inputType != MFInput.Type.DATE && inputType != MFInput.Type.TIME && inputType != MFInput.Type.DATETIME) {
			PropertyMetadata defaultValueMetadata = new PropertyMetadata(i18n.getLabel(language,
					"web.editor.properties.defaultValue"), true, editable, t, 9);
			//TODO we need the subtype here in order to detect if the defaultValue should be an INTEGER, STRING OR NUMERIC 
			metadata.put("defaultValue", defaultValueMetadata);
			
			if (inputType != MFInput.Type.EXTERNAL_LINK) {
				PropertyMetadata readOnlyMetadata = new PropertyMetadata(i18n.getLabel(language,
						"web.editor.properties.readOnly"), true, editable, PropertyMetadata.Type.BOOLEAN, 8);
				
				metadata.put("readOnly", readOnlyMetadata);	
			}

			if (inputType != MFInput.Type.EMAIL && inputType != MFInput.Type.EXTERNAL_LINK) {
				String minLengthLabel = (inputType == MFInput.Type.TEXT || inputType == MFInput.Type.TEXTAREA) ? "web.editor.properties.minlength"
						: "web.editor.properties.minvalue";
				String maxLengthLabel = (inputType == MFInput.Type.TEXT || inputType == MFInput.Type.TEXTAREA) ? "web.editor.properties.maxlength"
						: "web.editor.properties.maxvalue";

				PropertyMetadata minLengthMetadata = new PropertyMetadata(i18n.getLabel(language, minLengthLabel), true,
						editable, PropertyMetadata.Type.INTEGER, 7);
				metadata.put("minLength", minLengthMetadata);

				PropertyMetadata maxLengthMetadata = new PropertyMetadata(i18n.getLabel(language, maxLengthLabel), true,
						editable, PropertyMetadata.Type.INTEGER, 6);
				metadata.put("maxLength", maxLengthMetadata);	
			}

		}
		// El chiste de tener los metadatos acá es poder agregar propiedades
		// sin tocar js del editor
		//danicricco: That is a funny joke !, jejejeje
		return metadata;
	}
}
