import React from 'react';
import {render} from 'react-dom';
import injectTapEventPlugin from 'react-tap-event-plugin';
import Application from "./Application"; // Our custom react component

injectTapEventPlugin();

render(<Application/>, document.getElementById('app'));
