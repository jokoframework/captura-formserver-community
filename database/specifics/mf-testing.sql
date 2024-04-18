-- Customizations updates the standard values

UPDATE sys.parameters set value = 'en' where id = 1000;
UPDATE sys.parameters set value = 'https://mf-testing.hq.sodep.com.py/mf/' where id = 1002;
UPDATE sys.parameters set value = 'mobileforms@sodep.com.py' where id = 1003;
UPDATE sys.parameters set value = 'false' where id = 1004;
UPDATE sys.parameters set value = 'false' where id = 1015;--logout after an unexpected error
UPDATE sys.parameters set value = 'mobileforms@sodep.com.py' where id = 1019;
UPDATE sys.parameters set value = 'http://mf-testing.hq.sodep.com.py:8280/mf/api/document/upload/file?handle={handle}', active = true where id = 1021;
