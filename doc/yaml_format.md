# The openLCA YAML data format
The openLCA core module contains functions to read data sets from 
[YAML](http://www.yaml.org/) files. The aim of the format is to get (test) data 
quickly into openLCA with a human readable format without syntax ceremonies. 
This format is described in the following sections.

## Basic concepts

### Multiple data set types

### Anchors and references

### (TODO: Includes)

### Minimal required information


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




