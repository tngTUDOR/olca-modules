The openLCA YAML data format
============================
The openLCA core module contains functions to read data sets from 
[YAML](http://www.yaml.org/) files. The aim of the format is to get (test) data 
quickly into openLCA with a human readable format with minimal syntax elements. 
This format is described in the following sections.

## Idea and basic concepts
The idea is to define LCA models in single, text based documents in a very 
simple format that has syntax highlighting and editing support in text editors, 
is version control friendly, and can be easily parsed in standard programming 
languages. 

To be compatible with openLCA (but also with other LCA data formats like 
EcoSpold or ILCD) LCA models are stored in several data set types and a document
just contains a list of data sets:

```yaml
- unitGroup:
    name: Units of mass

- quantity:
    name: Mass

- flow:
    name: Steel
```

A data set is an object with an attribute that defines the data set type (e.g.
`unitGroup`, `flow`, `process`, etc.). Every data set should have a name and can
also have an UUID that can be used to link the data set to existing models. And
of course the format can be easily extended with custom attributes (like 
`sameAs` in the following example) and data sets:

```yaml
- unitGroup:
    name: Units of mass
    uuid: 93a60a57-a4c8-11da-a746-0800200c9a66
    sameAs: http://eplca.jrc.ec.europa.eu/ELCD3/resource/unitgroups/93a60a57-a4c8-11da-a746-0800200c9a66
```

A data set can be referenced from another data set by its name:

```yaml
- quantity:
    name: Mass
    unitGroup: Units of mass
```

Additionally, the format supports anchors (`&Units_of_mass`) and references 
(`*Units_of_mass`) as defined in the YAML format for referencing data sets and 
objects:

```yaml
- unitGroup: &Units_of_mass
    name: Units of mass
    
- quantity:
    name: Mass
    unitGroup: *Units_of_mass
```

This is useful for referencing objects that do not have a name (like an input
or output) or data sets where the name is not unique (in this case the data set
should also have a UUID). Note that an object are data set can be only 
referenced when it is already defined.

TODO: categories, includes (documents in documents)


## Data set types

### Unit groups
The following example shows an unit group data set. Basically, a unit group 
data set just contains a set of units that can be converted into each other
with a conversion factor. One unit of the unit group data set is the reference
unit with a conversion factor of 1. Note that the reference unit is defined 
after the definition of the units because it just points to a unit via a 
reference:

```yaml
- unitGroup: &Units_of_mass
    uuid: 91d1919d-e58f-498b-b620-b3e2694f4f1d
    name: Units of mass
    description: Description of units of mass 
    units:
      - &kg
        name: kg
        factor: 1.0
      - &g
        name: g
        factor: 0.001
    refUnit: *kg
```

If no conversion factor is given, the factor is set to 1. And if no reference
unit is defined the reference unit is set to the first unit with a conversion
factor of 1. So this is also a valid unit group data set:

```yaml
- unitGroup: &Units_of_mass
  name: Units of mass
  units:
    - &kg
      name: kg
```

### Quantities
Quantities are the same thing as flow properties in openLCA and the ILCD format:
they just point to an unit group:

```yaml
- quantity:
    &Mass
    name: Mass
    unitGroup: *Units_of_mass
```

The reason we have quantities is that different quantities can point to the same
unit group (e.g. mass and dry mass, or lower and upper calorific value) and we
can define conversions between such quantities in flows (see below).

### Flows
The following example shows a flow data set:

```yaml
- flow:
    &Steel
    type: product
    name: Steel
    refQuantity: *Mass
```

Flows can be used in inputs and outputs of processes. The type of a flow 
indicates if and how a flow can be linked in a product system. Allowed are the
following type values:

* `product`: product flows can be linked in a product system from a process that
  produces the product to a process that consumes the product in an input.
* `waste`: waste flows can be linked in a product system from a process that 
  produces waste to a process that treats that waste in an input.
* `elementary`: elementary flows cannot be linked

TODO: quantities in flows

### Processes

```yaml
- process: &ABS_process
    name: ABS
    type: lci
    inputs:
      - flow: *Crude_oil
        amount: 0.98
        unit: *kg
    outputs:
      - &ABS_process_product
        flow: *ABS_product
        amount: 1
        unit: *kg
      - flow: *CO2
        amount: 3.05
        unit: *kg
    refFlow: *ABS_process_product
```

#### Process type
The `type` field of the process can have the values `lci` (LCI result) or `unit`
(unit process). If the field is missing the process will be classified as an
unit process.

