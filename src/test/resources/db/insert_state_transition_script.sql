-- El valor del campo form_id debería ser el del formulario correspondiente en forms.forms

-- Estados
INSERT INTO workflow.states(
            id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado inicial del documento', 1, 'Pendiente', true);

INSERT INTO workflow.states(
            id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el Coordinador considera que esta correcto', 1, 'Recepcionado Coord', false);

INSERT INTO workflow.states(
            id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el CIA considera que esta correcto', 1, 'Recepcionado CIA', false);

INSERT INTO workflow.states(
	    id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el Coordinador considera que hay un error', 1, 'Rechazado', false);

INSERT INTO workflow.states(
	    id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el CIA considera que hay un error', 1, 'Rechazado CIA', false);

INSERT INTO workflow.states(
	    id, description, form_id, name, initial)
    VALUES ((SELECT nextval('workflow.seq_states')), 'Estado al que pasa el documento cuando el Coordinador verifica que ya se ha vuelto a digitalizar', 1, 'Anulado', false);

-- Transiciones
INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición al estado Pendiente cuando se digitaliza un documento', 1, null, (SELECT id FROM workflow.states WHERE name = 'Pendiente'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Pendiente al estado Recepcionado Coord', 1, (SELECT id FROM workflow.states WHERE name = 'Pendiente'), (SELECT id FROM workflow.states WHERE name = 'Recepcionado Coord'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Pendiente al estado Rechazado', 1, (SELECT id FROM workflow.states WHERE name = 'Pendiente'), (SELECT id FROM workflow.states WHERE name = 'Rechazado'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Recepcionado Coord al estado Recepcionado CIA', 1, (SELECT id FROM workflow.states WHERE name = 'Recepcionado Coord'), (SELECT id FROM workflow.states WHERE name = 'Recepcionado CIA'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Recepcionado Coord al estado Rechazado CIA', 1, (SELECT id FROM workflow.states WHERE name = 'Recepcionado Coord'), (SELECT id FROM workflow.states WHERE name = 'Rechazado CIA'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Rechazado CIA al estado Rechazado', 1, (SELECT id FROM workflow.states WHERE name = 'Rechazado CIA'), (SELECT id FROM workflow.states WHERE name = 'Rechazado'));

INSERT INTO workflow.transitions(
            id, description, form_id, origin_state, target_state)
    VALUES ((SELECT nextval('workflow.seq_transitions')), 'Transición del estado Rechazado al estado Anulado', 1, (SELECT id FROM workflow.states WHERE name = 'Rechazado'), (SELECT id FROM workflow.states WHERE name = 'Anulado'));