% fixes a value between minval and maxval
function [output] = clamp(arg, minval, maxval)
    output = min(maxval, max(minval, arg));
end
