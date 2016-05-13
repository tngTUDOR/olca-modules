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

```
- unitGroup: &Units_of_mass
  name: Units of mass
  units:
    - name: kg
```  