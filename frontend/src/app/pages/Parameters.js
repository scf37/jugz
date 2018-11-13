import React, {Component} from 'react';
import RaisedButton from 'material-ui/RaisedButton';
import TextField from 'material-ui/TextField';
import Validator from "../util/validator";

const buttonsStyle = {
    margin: 25
};


class Parameters extends Component {

    constructor(props, context) {
        super(props, context);


        this.state = {

        };

        this.validator = new Validator(this);
        this.validate = this.validator.validate;
    }

    componentDidMount() {

    };

    onSolve = () => {
        if (this.validator.checkInvalid()) return;
        this.props.ajax.post("/jugs/solve", {
            x: this.state.x,
            y: this.state.y,
            z: this.state.z,
        }).then(resp => {
            if (resp.data.message) {
                this.setState({
                    error: resp.data.message
                });
            } else {
                this.setState({
                    error: ""
                });
                this.props.onSolve(Object.assign({
                    x: this.state.x,
                    y: this.state.y,
                    z: this.state.z
                }, resp.data));
            }
        }).catch(err => {
            this.setState({
                error: err.message
            });
        });
    };

    onRegister = () => {
        this.setState({
            registerDialog: true
        })
    };

    registerUser = (user) => {
        this.props.ajax.post("/register", user).then((result) => {
            return this.props.ajax.login(user.login, user.password)
        }).then((result) => {
            this.props.onLogin(result.data)
        })
    };

    isValid = (v) => {
        const vv = +v;
        if (isNaN(vv) || vv < 1) return "Must be positive integer";
        if (vv !== Math.round(vv)) return "Must be positive integer";
        if (vv >= 0x80000000) return "Must be less than " + 0x80000000;

        return false;
    };

    render() {
        return (
            <div>
                <h1>Water Jug Riddle Solver</h1>
                <div>
                    <TextField
                        floatingLabelText="Volume of jug X"
                        value={this.state.x}
                        errorText={this.state.xError}
                        onChange={this.validate('x', 'xError', x => {
                            const err = this.isValid(x);
                            if (err) return err;
                            return ""
                        })}
                    />
                </div>
                <div>
                    <TextField
                        floatingLabelText={"Volume of jug Y"}
                        value={this.state.y}
                        errorText={this.state.yError}
                        onChange={this.validate('y', 'yError', x => {
                            const err = this.isValid(x);
                            if (err) return err;
                            return ""
                        })}
                    />
                </div>
                <div>
                    <TextField
                        floatingLabelText={"Volume to measure"}
                        value={this.state.z}
                        errorText={this.state.zError}
                        onChange={this.validate('z', 'zError', z => {
                            const err = this.isValid(z);
                            if (err) return err;
                            if (z > Math.max(this.state.x, this.state.y)) {
                                return "Volume to measure must fit into a jug";
                            }
                            return ""
                        })}
                    />
                </div>

                <div style={{paddingBottom: 45}}/>
                <RaisedButton
                    label={"Solve"}
                    style={buttonsStyle}
                    secondary={true}
                    onTouchTap={this.onSolve}
                />
                <h3>{this.state.error}</h3>

            </div>
        );
    }
}

export default Parameters;
