% PI Controller implementation
function [out, acum_error] = pid(pv, setpoint, p_gain, i_gain, acum_error)
    p = (setpoint - pv) * p_gain;
    ii = (setpoint - pv) * i_gain;
    acum_error = acum_error + ii;
    out = p + acum_error;
end
