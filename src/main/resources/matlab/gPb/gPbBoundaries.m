function gPbBoundaries(imagePath, boundariesPath)
addpath(genpath('.'));

gPb_orient = globalPb(imagePath, '');

% This is the boundary probability map.
ucm2 = contours2ucm(gPb_orient, 'doubleSize');

imwrite(ucm2, boundariesPath);
end

