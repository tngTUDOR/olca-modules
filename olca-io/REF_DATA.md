openLCA Reference Data and Mapping Format
=========================================
The openLCA reference data format defines a simple CSV format for exchanging
reference data like flows, units, categories etc. and mappings to entities of
other formats and databases. In general all CSV files should have the following
properties:

* files should be utf-8 encoded
* columns should be separated by semicolon: ;
* strings should be enclosed in double quotes if it is necessary: "
* the decimal separator of numbers should be a point: .
* the files should not contain column headers


Locations
---------
File:       `locations.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional) 
3. code (string, required)
4. latitude (double, required)
5. longitude (double, required)

Categories
----------
File:       `categories.csv`      
Columns:

0. reference ID (UUID; required)
1. name (string; required)
2. description (string; optional)
3. model type of the category (enumeration: "PROJECT", "PRODUCT_SYSTEM", 
   "IMPACT_METHOD", "PROCESS", "FLOW", "FLOW_PROPERTY", "UNIT_GROUP"; required)
4. reference ID of the parent category (UUID, optional)


Units
-----
File:       `units.csv`
Columns:

0. reference ID (UUID; required)
1. name (string; required)
2. description (string; optional)
3. conversion factor (double; required)
4. synonyms (string: list separated by semicolon; optional)
5. reference ID of unit group (UUID, required)


Unit groups
-----------
File:       `unit_groups.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)
4. default flow property ID (UUID, optional)
5. reference unit ID (UUID, required)


Flow properties
---------------
File:       `flow_properties.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)
4. unit group ID (UUID, required)
5. flow property type (integer enum: 0=economic, 1=physical; required)


Flows
-----
File:       `flows.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)
4. flow type (enumeration: 'ELEMENTARY_FLOW', 'PRODUCT_FLOW', 'WASTE_FLOW'; required)
5. CAS number (string, optional)
6. formula (string, optional)
7. reference flow property (UUID, required)


Flow property factors 
---------------------
(relations between flows and flow properties)
File:       `flow_property_factors.csv`

0. flow ID (UUID, required)
1. flow property ID (UUID, required)
2. factor (double, required) (1 if it is the reference flow property)


LCIA methods
------------
File:       `lcia_methods.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. category ID (UUID, optional)


LCIA categories
---------------
File:       `lcia_categories.csv`
Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. reference unit (string, required)
4. LCIA method ID (UUID, required)


LCIA factors
------------
File:       `lcia_factors.csv`

Columns:

0. reference ID of the LCIA category (UUID, required)
1. reference ID of the flow (UUID, required)
2. reference ID of the flow property of the factor (UUID, required)
3. reference ID of the unit of the factor (UUID, required)
4. value of the factor (double, required)


Normalisation and weighting sets
--------------------------------
File:		`nw_sets.csv`

Columns:

0. reference ID (UUID, required)
1. name (string, required)
2. description (string, optional)
3. weighted score unit (string, optional)
4. LCIA method ID (UUID, required)


Normalisation and weighting set factors
---------------------------------------
File:		`new_set_factors.csv`

Columns:

0. reference ID of the normalisation and weighting set (UUID, required)
1. reference ID of the LCIA category (UUID, required)
2. nomalisation factor (double, optional)
3. weighting factor (double, optional)


Default mappings
================


Default mappins: Unit import
----------------------------
File:		`unit_import_map.csv`

Columns:

0. external name of the unit
1. openLCA reference ID of the unit (UUID)
2. openLCA name of the unit (string)
3. openLCA reference ID of the flow property (UUID)
4. openLCA name of the flow property


EcoSpold 1
==========

* units are mapped by name (see default unit mapping) 
* locations are mapped by the location code (name-based UUID)


EcoSpold 1: Flow import mapping
-------------------------------

0. EcoSpold name of the flow (string)
1. EcoSpold category of the flow (string)
2. EcoSpold sub-category of the flow (string)
3. EcoSpold unit of the flow (string)
4. openLCA reference ID of the flow (UUID)
5. openLCA name of the flow (string)
6. openLCA reference ID of the reference flow property of the flow (UUID)
7. openLCA name of the reference flow property of the flow (string)
8. openLCA reference ID of the reference unit of the flow (UUID)
9. openLCA name of the reference unit of the flow (string)
10. conversion factor: amount_ecospold * factor = amount_openlca (double)


EcoSpold 2
==========


EcoSpold 2: Unit import mapping
-------------------------------
File:		`es2_unit_import_map.csv`

Columns:

0. ecoinvent reference ID of the unit (UUID)
1. ecoinvent name of the unit (string)
2. openLCA reference ID of the unit (UUID)
3. openLCA name of the unit (string)
4. openLCA reference ID of the flow property (UUID)
5. openLCA name of the flow property


EcoSpold 2: Flow import mapping
-------------------------------
File:		`es2_flow_import_map.csv`

Columns:

0. ecoinvent reference ID of the flow (UUID)
1. ecoinvent name of the flow (string)
2. ecoinvent reference ID of the reference unit of the flow (UUID)
3. ecoinvent name of the reference unit of the flow (string)
4. openLCA reference ID of the flow (UUID)
5. openLCA name of the flow (string)
6. openLCA reference ID of the reference flow property of the flow (UUID)
7. openLCA name of the reference flow property of the flow (string)
8. openLCA reference ID of the reference unit of the flow (UUID)
9. openLCA name of the reference unit of the flow (string)
10. conversion factor: amount_ecoinvent * factor = amount_openlca (double)


EcoSpold 2: Location export mapping
-----------------------------------
File:       `es2_location_export_map.csv`

Columns:

0. openLCA reference ID of the location (UUID)
1. openLCA location code of the location (string)
2. ecoinvent reference ID of the location (UUID)
3. ecoinvent location code of the location (string)


EcoSpold 2: Unit export mapping
-------------------------------
File:       `es2_unit_export_map.csv`

0. openLCA reference ID of the unit (UUID)
1. openLCA name of the unit (string)
2. ecoinvent reference ID of the unit (UUID)
3. ecoinvent name of the unit (string)


EcoSpold 2: Flow export mapping
-------------------------------
File:       `es2_flow_export_map.csv`

0. openLCA reference ID of the flow (UUID)
1. openLCA name of the flow (string)
2. openLCA reference ID of the reference flow property of the flow (UUID)
3. openLCA name of the reference flow property of the flow (string)
4. openLCA reference ID of the reference unit of the flow (UUID)
5. openLCA name of the reference unit of the flow (string)
6. ecoinvent reference ID of the flow (UUID)
7. ecoinvent name of the flow (string)
8. ecoinvent reference ID of the reference unit of the flow (UUID)
9. ecoinvent name of the reference unit of the flow (string)
10. conversion factor: amount_openlca * factor = amount_ecoinvent (double)


ILCD
====

* we use the same UUIDs for unit groups and flow properties as in the
  official ILCD package


ILCD: Flow import mapping
-------------------------

0. ILCD reference ID of the flow (UUID)
1. ILCD name of the flow (string)
2. ILCD reference ID of the reference flow property of the flow (UUID)
3. ILCD name of the reference flow property of the flow (string)
4. ILCD reference ID of the reference unit group of the flow (UUID)
5. ILCD name of the reference unit of the flow (string)
6. openLCA reference ID of the flow (UUID)
7. openLCA name of the flow (string)
8. openLCA reference ID of the reference flow property of the flow (UUID)
9. openLCA name of the reference flow property of the flow (string)
10. openLCA reference ID of the reference unit of the flow (UUID)
11. openLCA name of the reference unit of the flow (string)
12. conversion factor: amount_ilcd * factor = amount_openlca (double)


SimaPro CSV
===========

* units are mapped by name (see default unit mapping) 


SimaPro CSV: Compartment import mapping
---------------------------------------
File:		`sp_compartment_import_map.csv`

Columns:

0. SimaPro compartment name (string)
1. SimaPro sub-compartment name (string)
2. openLCA reference ID of the category (UUID)
3. openLCA short category path category/sub-category (string)


SimaPro CSV: Flow import mapping
--------------------------------
File:		`sp_flow_import_map.csv`

Columns:

0. SimaPro name of the flow (string)
1. SimaPro compartment of the flow (string)
2. SimaPro sub-compartment of the flow (string)
3. SimaPro unit of the flow (string)
4. openLCA reference ID of the flow (UUID)
5. openLCA name of the flow (string)
6. openLCA reference ID of the reference flow property of the flow (UUID)
7. openLCA name of the reference flow property of the flow (string)
8. openLCA reference ID of the reference unit of the flow (UUID)
9. openLCA name of the reference unit of the flow (string)
10. conversion factor: amount_simapro * factor = amount_openlca (double)
