function [ops, credits] = readAndPlotOneCreditFile()
%this function parses one of the .credit files created by mopAOS and plots
%the credit history. It also returns the credit history for each operator.
%ops are the operator names, credits is a two column vector for each
%operator


path = '/Users/nozomihitomi/Dropbox/EOSS/problems/climateCentric/result/AIAA JAIS/';
respath = strcat(path,'random');
origin = cd(respath);

files = dir('*.credit');
allcredits  = cell(length(files),1);
ops_set = java.util.HashSet;
for i=1:length(files)
    expData = java.util.HashMap;
    fid = fopen(files(i).name,'r');
    while(feof(fid)==0)
        line = fgetl(fid);
        [~, endIndex] = regexp(line,'iteration,');
        raw_iteration = strsplit(line(endIndex+1:end),',');
        %need to split out the operator name
        line = fgetl(fid);
        [startIndex, endIndex] = regexp(line,'[A-z\+]+,');
        raw_credits = strsplit(line(endIndex+1:end),',');
        op_data = zeros(length(raw_iteration),2);
        for j=1:length(raw_credits)
            op_data(j,1)=str2double(raw_iteration{j}); %iteration
            op_data(j,2)=str2double(raw_credits{j}); %credit
        end
        %sometimes there is 0 iteration selection which is not valid
        op_data(~any(op_data(:,1),2),:)=[];
        expData.put(line(startIndex:endIndex-1),op_data);
        
        %record the operator names
        ops_set.add(line(startIndex:endIndex-1));
    end
    fclose(fid);
    allcredits{i} = expData;
end

%get operator names
iter = ops_set.iterator;
ops = cell(expData.size,1);
i = 1;
while(iter.hasNext)
    ops{i} = iter.next;
    i = i + 1;
end
ops = sort(ops);
numOps = length(ops);

%plot
nepochs = 100;

maxEval = 5000;
epochLength = maxEval/nepochs;
all_epoch_credit = zeros(expData.keySet.size, nepochs, length(files)); %keeps track of the epoch credits from the operators
all_epoch_select = zeros(expData.keySet.size, nepochs, length(files)); %keeps track of the epoch selection count for the operators

for i=1:length(files)
    for k = 1:numOps
        if(strcmp(op,'OnePointCrossover+BitFlip'))
            continue;
        end
        hist = allcredits{i}.get(ops{k});
        if size(hist,1)==0
            %means that the opeator was never selected
            continue;
        elseif size(hist,1) == 2 && size(hist, 2) == 1
            %sometimes the row vector gets flipped to column vector
            hist = hist';
        end
        %sepearates out credits into their respective epochs
        for j=1:nepochs
            %find indices that lie within epoch
            ind1 = epochLength*(j-1)<hist(:,1);
            ind2 = hist(:,1)<epochLength*j;
            epoch = hist(and(ind1,ind2),:);
            if numel(epoch) == 1
                disp('a');
            end
            if(~isempty(epoch(:,1))) %if it is empty then operator was not selected in the epoch
                all_epoch_credit(k, j, i) = mean(epoch(:,2));
                all_epoch_select(k, j, i) = length(unique(epoch(:,1)));
            end
        end
    end
end


colors = {
      [0         0.4470    0.7410]
    [0.8500    0.3250    0.0980]
    [0.9290    0.6940    0.1250]
    [0.4940    0.1840    0.5560]
    [0.4660    0.6740    0.1880]
    [0.3010    0.7450    0.9330]
    [0.6350    0.0780    0.1840]};

figure(1)
cla
handles = [];
maxCredit = 0;
for i=1:numOps
    X = [1:nepochs,fliplr(1:nepochs)];
    stddev = std(squeeze(all_epoch_credit(i,:,:)),0,2);
    mean_cred = mean(squeeze(all_epoch_credit(i,:,:)),2);
    Y = [mean_cred-stddev;flipud(mean_cred+stddev)];
    Y(Y<0) = 0; %correct for negative values
    %     fill(X,Y,colors{i},'EdgeColor','none');
    alpha(0.15)
    hold on
    handles = [handles plot(1:nepochs,mean_cred,'Color',colors{i}, 'LineWidth',2)];
    maxCredit = max([max(mean_cred), maxCredit]);
end
hold off
set(gca,'FontSize',16);
axis([0,nepochs, 0, maxCredit*1.1])
set(gca,'XTick',0:nepochs/10:nepochs);
set(gca,'XTickLabels',0:nepochs/10*epochLength:nepochs*epochLength);
xlabel('NFE')
ylabel('Credit earned')
legend(handles, ops);

figure(2)
cla
handles = [];
%normalize the selection to make it a probability
means = mean(all_epoch_select,3);
mean_sum = sum(means,1);

for i=1:numOps
    mean_sel = means(i,:)./mean_sum;
    hold on
    handles = [handles, plot(2:nepochs,mean_sel(2:end),'Color',colors{i}, 'LineWidth',2)];
end
plot([0,5000],[0.03,0.03],'--k')
legend(handles, ops);
axis([0, nepochs, 0, 1])
xlabel('NFE')
ylabel('Selection frequency')
set(gca,'XTick',0:nepochs/10:nepochs);
set(gca,'XTickLabels',0:nepochs/10*epochLength:nepochs*epochLength);
hold off
set(gca,'FontSize',16);
%save files
save('credit.mat','allcredits');

cd(origin);


end