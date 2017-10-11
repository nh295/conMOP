function [V,B] = launch_deltaV(lat, inc, alt)
%this function uses equations 6-42 to 6-45 from SMAD edition 3 (1999) to
%compute the deltav required to get a spacecraft from the launch site at a
%specific latitude to the deired orbit at a specific inclination and
%altitude
%
%Inputs: 
% lat: launch site latitude [rad]
% inc: orbit inclination [rad]
% alt: altitude [m]
%
%Outputs:
% V: delta v required. If more than one launch azimuth is available, V will
% be a vector of velocities
% B: launch azimuths

%constants
G = 6.673e-11; %gravity constant [N m^2/kg^2]
me = 5.972e24; %earth mass [kg]
re = 6371900; %earth radius [m]

%no launch window exists if lat > inc for prograde
if(inc <= pi/2 && lat > inc)
    V = [];
    B = [];
    return
elseif(inc > pi/2 && lat < pi/2 - inc)
    %no launch window exists if lat < pi/2 - inc for retrograde
    V = [];
    B = [];
    return
end

%Veq is the inertial velocity at the equator
Veq = 464.5;
%VL is inertial velocity of launch site. eq (6-46)
VL = Veq * cos(lat);
%V0 is velocity of satellite immediately after launch ~= 7.8km/s
V0 = 7800000;
%Vbo is the velocity at burnout (circular orbital velocity at altitude)
Vbo = sqrt(G*me/(re+alt));

%inertial launch azimuth BI eq (6-42b)
BI = asin(cos(inc)/cos(lat));
%correct for velocity contribution caused by Earth's rotation eq (6-42c)
gam = atan((VL*cos(BI))/(V0-Veq*cos(inc)));
%compute launch azimuth eq (6-42a)
B = [BI+gam, BI-gam];

%compute velocity components in topcentric-horizon corrdinates
V = zeros(1,2);
flt_path_ang = 0;
for i=1:2
    VS = -Vbo*cos(flt_path_ang)*cos(B(i));
    VE = Vbo*cos(flt_path_ang)*sin(B(i))-VL;
    VZ = Vbo*sin(flt_path_ang);
    V(i)=norm([VS,VE,VZ]);
end
