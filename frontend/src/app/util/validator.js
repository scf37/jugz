/**
 * Validation helper, provides component-wide validation of input.
 *
 * Usage:
 *
 * this.validator = new Validator(this)
 * this.validate = this.validator.validate
 * // ...
 * <MyComponent
 *  value={this.state.myValue}
 *  error={this.state.myError}
 *  onChange={validate('myValue', 'myError', value => {
 *      if (value == '') return "value cannot be empty"
 *      return ""
 *  })} />
 *
 */
class Validator {
    constructor(parentComponent) {
        this.parent = parentComponent;
        this.parent.state.validators = {};
    }

    /**
     * Produce onChange handler for validated component
     *
     * @param valueName state value name for component's value
     * @param valueErrorName state value name for component's error string
     * @param validationFunc takes new value, outputs error text or '' if no error
     * @returns {function(*, *=)} typical (event, value) => void error handler
     */
    validate = (valueName, valueErrorName, validationFunc) => {
        const validator = (noEvent, value) => {
            let error;
            try {
                error = validationFunc(value);
            } catch (e) {
                error = e.message
            }
            const st = {};
            st[valueName] = value;
            st[valueErrorName] = error;
            this.parent.setState(st);
            return error !== ""
        };

        this.parent.state.validators[valueName] = () => {
            return validator(null, this.parent.state[valueName])
        };

        return validator;
    };

    /**
     * re-run existing validations
     * @returns {boolean} true if there are errors
     */
    checkInvalid = () => {
        let hasError = false;
        const validators = this.parent.state.validators;
        for (let v in validators) {
            if (validators.hasOwnProperty(v)) {
                hasError |= validators[v]();
            }
        }

        return hasError;
    }

}

export default Validator;