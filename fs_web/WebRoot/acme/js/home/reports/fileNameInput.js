define( ["jquery", "acme" ], function($, acme) {

    // TODO This package should probably be a jQuery widget
    // i.e. and input augmented with filename autocomplete
    // and other functionality

    var pkg = {};

    var searchElem = function(term, elements) {
        var arr = [], e, t, label;
        for (var i = 0; i < elements.length; i++) {
            e = elements[i];
            label = e.label.toLowerCase();
            t = term.toLowerCase();
            if (label.indexOf(t) > -1) {
                arr.push(e);
            }
        }
        return arr;
    };

    function extractLast( term ) {
        return split(term).pop();
    };

    function split(value) {
        return value.split("[");
    };

    pkg.utils = {
        // util functions
        regexpEncode: function(str) {
            return str.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
        },

        // constructs an array of elements with unique labels
        toArrayOfUniqueLabels: function(elementList) {
            var labels = [], labelCounts = {};

            var uniqueElements = $.map(elementList, function (elem, index) {
                var uniqueLabel = '', foundIdx;

                if (elem.id.startsWith("meta_")) {
                    return null;
                }

                // we search for repeated labels
                // and if found, we concatenate an '(index)'
                foundIdx = $.inArray(elem.label, labels);
                if (foundIdx > -1) {
                    labelCounts[foundIdx] = (labelCounts[foundIdx] || 0) + 1;
                    uniqueLabel = elem.label + "(" + labelCounts[foundIdx] + ")";
                } else {
                    labels.push(elem.label);
                    uniqueLabel = elem.label;
                }

                return {label: uniqueLabel, id: elem.id};
            });

            return uniqueElements;
        }
    };

    pkg.autocompleteSettings = {
        makeSourceHandler: function (elements) {
            return function (request, response) {
                var term = extractLast(request.term);
                var data = searchElem(term, elements);
                response(data);
            };
        },
        searchHandler: function () {
            if (this.value.indexOf("[") < 0) {
                return false;
            }
            // custom minLength
            var term = extractLast(this.value);
            if (term.length < 1) {
                return false;
            }
        },
        focusHandler: function () {
            return false;
        },
        makeSelectHandler: function (replacement, hidden, elements) {
            return function (event, ui) {
                var terms = split(this.value);

                // remove the current input
                terms.pop();

                // add the selected item
                terms.push(ui.item[replacement] + "]");

                this.value = terms.join("[");

                pkg.updateSyntaxValues(this, hidden, elements, ui.item.id);

                return false;
            };
        },
        keyDownHandler: function (event) {
            // don't navigate away from the field on tab when selecting an item
            if (event.keyCode === $.ui.keyCode.TAB &&
                $(this).data("autocomplete").menu.active) {
                event.preventDefault();
            }
        }
    };

    // We update the hidden inputs with the required
    // [elementId] file names syntax
    pkg.updateSyntaxValues = function (textBoxSource, textBoxTarget, elements, elementId) {
        var syntaxMessage = $.trim(textBoxSource.value);
        $.each(elements, function (index, elem) {
            var textSyntax = "[" + elem.id + "]";
            syntaxMessage = syntaxMessage.replace(new RegExp(pkg.utils.regexpEncode("[" + elem.label + "]"), 'g'), textSyntax);
        });
        $(textBoxTarget).val(syntaxMessage);
    };

    pkg.getFileNameByElement = function(elementId, elementsFileNames) {
        if (!elementsFileNames) {
            return null;
        }
        return elementsFileNames[elementId];
    };

    return pkg;
});
