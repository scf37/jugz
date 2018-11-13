import React, {Component} from 'react';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import {deepOrange500} from 'material-ui/styles/colors';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import Ajax from "./util/ajax";
import Parameters from "./pages/Parameters";
import Solution from "./pages/Solution";
import RaisedButton from "material-ui/RaisedButton";


const muiTheme = getMuiTheme({
    palette: {
        accent1Color: deepOrange500,
    },
});

const styles = {
    container: {
        textAlign: 'center',
        paddingTop: 20,
    },
};

const navContainer = {};

const navLink = {
    fontSize: 24,
    padding: 10
};

class Application extends Component {
    constructor(props, context) {
        super(props, context);

        this.state = {
            user: null,
            params: true,
            solution: false,
            x: 3,
            y: 5,
            z: 4,
            firstJug: false,
            count: 6

        };

        this.ajax = new Ajax()
    }

    solve(result) {
        // result.count
        // result.firstJug
        console.log(result);
        this.setState({
            x: result.x,
            y: result.y,
            z: result.z,
            firstJug: result.firstJug,
            count: result.count,
            params: false,
            solution: true
        })
    }

    onSolveAnother() {
        this.setState({
            params: true,
            solution: false
        })
    }

    render() {
        return (
            <div>
                    <MuiThemeProvider muiTheme={muiTheme}>
                        <div style={styles.container}>
                            {this.state.params && <Parameters ajax={this.ajax} onSolve={r => this.solve(r)}/>}
                            {this.state.solution && <div>
                                <RaisedButton
                                    label={"Solve another one"}
                                    secondary={true}
                                    onTouchTap={() => this.onSolveAnother()}
                                />
                                <Solution ajax={this.ajax}
                                          x={this.state.x} y={this.state.y} z={this.state.z}
                                          firstJug={this.state.firstJug} count={this.state.count} firstAction={"Ready!"}/>
                            </div>}
                        </div>

                    </MuiThemeProvider>
            </div>
        );
    }
}

export default Application;
