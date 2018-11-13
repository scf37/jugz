import React, {Component} from 'react';
import FlatButton from "material-ui/FlatButton";
import ArrowBack from "material-ui/svg-icons/navigation/arrow-back";
import {amber50} from "material-ui/styles/colors";
import ArrowForward from "material-ui/svg-icons/navigation/arrow-forward";

const navButtonDisabled = {
    backgroundColor: "lightgray"
};

const actionStyle = {
    fontSize: "150%",
    width: "20em",
    verticalAlign: "middle",
    display: "inline-block"
};

const jug = {
    border: "2px solid #74ccf4",
    borderTopWidth: "0",
    width: "100px",
    height: "160px",
    position: "relative",
    display: "inline-block"
};

const jugWater ={
    left: "0",
    top: "160px",
    width: "100px",
    height: "0px",
    background: "linear-gradient(to bottom, rgb(90,188,216), rgb(15,94,156))",
    position: "absolute",
    transition: "0.3s all ease-in"
};


class Solution extends Component {

    constructor(props, context) {
        super(props, context);

        this.state = this.startState()
    }

    getNext = (vx, vy) => {
        return this.props.ajax.post("/jugs/next", {
            x: this.props.x,
            y: this.props.y,
            z: this.props.z,
            state: {
                vx: vx,
                vy: vy
            },
            firstJug: this.props.firstJug
        });
    };

    startState() {
        return {
            jugWaterX: Object.assign({}, jugWater),
            jugWaterY: Object.assign({}, jugWater),
            vx: 0,
            vy: 0,
            actionN: 0,
            actionText: this.props.firstAction,
            working: false,
            history: []
        };
    }

    componentDidMount() {
        this.setState(this.startState())
    };

    navNext = () => {
        this.setState({
            working: true
        });
        this.getNext(this.state.vx, this.state.vy).then(r => {
            this.state.history.push([this.state.actionText, this.state.vx, this.state.vy]);

            const vx = r.data.nextState.vx;
            const vy = r.data.nextState.vy;

            this.updateState(vx, vy, r.data.action, this.state.actionN + 1);
        }).catch(e => {
            this.setState({
                working: false
            });
            alert(e.message);
        });
    };

    updateState = (vx, vy, action, actionN) => {
        const waterX = Object.assign({}, jugWater);
        const waterY = Object.assign({}, jugWater);

        waterX.top = 160 * (1 - vx / this.props.x) + "px";
        waterX.height = 160 * vx / this.props.x + "px";

        waterY.top = 160 * (1 - vy / this.props.y) + "px";
        waterY.height = 160 * vy / this.props.y + "px";

        this.setState({
            working: false,
            actionN: actionN,
            actionText: action,
            vx: vx,
            vy: vy,
            jugWaterX: waterX,
            jugWaterY: waterY
        })
    };

    navPrev = () => {
        let lastState = this.state.history.pop();
        this.updateState(lastState[1], lastState[2], lastState[0], this.state.actionN - 1);
    };

    render() {
        return (
            <div>
                <h2>Given Jug X of {this.props.x} gal and Jug Y of {this.props.y} gal, measure {this.props.z} gal.</h2>
                <div>
                    <div>
                        <div>
                            <div>
                                <FlatButton
                                    backgroundColor="#a4c639"
                                    hoverColor="#8AA62F"
                                    icon={<ArrowBack color={amber50}/>}
                                    disabled={this.state.actionN === 0 || this.state.working}
                                    style = {
                                        this.state.actionN === 0 || this.state.working ? navButtonDisabled: {}
                                    }
                                    onClick={this.navPrev}
                                />
                                <span style={actionStyle}>
                                    {this.state.actionN === 0 ? "Ready!" : `Action ${this.state.actionN} of ${this.props.count}`}
                                    </span>
                                <FlatButton
                                    backgroundColor="#a4c639"
                                    hoverColor="#8AA62F"
                                    icon={<ArrowForward color={amber50}/>}
                                    disabled={this.state.actionN === this.props.count || this.state.working}
                                    style = {
                                        this.state.actionN === this.props.count || this.state.working ? navButtonDisabled: {}
                                    }
                                    onClick={this.navNext}
                                />
                            </div>
                            <h3>{this.state.actionText}</h3>
                        </div>
                    </div>
                </div>
                <div style={{display:"flex", flexDirection:"row", justifyContent: "center"}}>
                    <span style={{paddingRight:"5em", width: "15em"}}>
                        Jug X <br/>
                        <div id="jug-y" style={jug}>
                            <div style={this.state.jugWaterX}>&nbsp;</div>
                        </div> <br/>
                        {this.state.vx}/{this.props.x} gal
                    </span>
                    <span style={{paddingRight:"5em", width: "15em"}}>
                        Jug Y <br/>
                        <div id="jug-y" style={jug}>
                            <div style={this.state.jugWaterY}>&nbsp;</div>
                        </div> <br/>
                        {this.state.vy}/{this.props.y} gal
                    </span>
                </div>
            </div>
        );
    }
}

export default Solution;
