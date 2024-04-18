-- El valor del campo form_id debería ser el del formulario correspondiente en forms.forms

-- Estados
INSERT INTO workflow.states(
            id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado inicial del documento', :formId, 'Pendiente', true);

INSERT INTO workflow.states(
            id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el Coordinador considera que esta correcto', :formId, 'Recepcionado Coord', false);

INSERT INTO workflow.states(
            id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el CIA considera que esta correcto', :formId, 'Recepcionado CIA', false);

INSERT INTO workflow.states(
	    id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el Coordinador considera que hay un error', :formId, 'Rechazado', false);

INSERT INTO workflow.states(
	    id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el CIA considera que hay un error', :formId, 'Rechazado CIA', false);

INSERT INTO workflow.states(
	    id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el Coordinador verifica que ya se ha vuelto a digitalizar', :formId, 'Anulado', false);

-- Transiciones
INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición al estado Pendiente cuando se digitaliza un documento', :formId, null, (SELECT id FROM workflow.states WHERE name = 'Pendiente'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Pendiente al estado Recepcionado Coord', :formId, (SELECT id FROM workflow.states WHERE name = 'Pendiente'), (SELECT id FROM workflow.states WHERE name = 'Recepcionado Coord'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Pendiente al estado Rechazado', :formId, (SELECT id FROM workflow.states WHERE name = 'Pendiente'), (SELECT id FROM workflow.states WHERE name = 'Rechazado'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Recepcionado Coord al estado Recepcionado CIA', :formId, (SELECT id FROM workflow.states WHERE name = 'Recepcionado Coord'), (SELECT id FROM workflow.states WHERE name = 'Recepcionado CIA'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Recepcionado Coord al estado Rechazado CIA', :formId, (SELECT id FROM workflow.states WHERE name = 'Recepcionado Coord'), (SELECT id FROM workflow.states WHERE name = 'Rechazado CIA'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Rechazado CIA al estado Rechazado', :formId, (SELECT id FROM workflow.states WHERE name = 'Rechazado CIA'), (SELECT id FROM workflow.states WHERE name = 'Rechazado'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Rechazado al estado Anulado', :formId, (SELECT id FROM workflow.states WHERE name = 'Rechazado'), (SELECT id FROM workflow.states WHERE name = 'Anulado'));

-- asignamos roles a transiciones y estados

INSERT INTO workflow.states_roles(
	state_id, role_id)
	VALUES ((select id from workflow.states where initial = true and form_id = :formId), (SELECT id from core.roles where name = 'ROL_WORKFLOW_ADMIN'));

INSERT INTO workflow.transitions_roles(
	transition_id, role_id)
	VALUES ((select id from workflow.transitions where origin_state is null and form_id = :formId) , (SELECT id from core.roles where name = 'ROL_WORKFLOW_ADMIN'));