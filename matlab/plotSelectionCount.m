function plotSelectionCount
%plot stacked bar graph for the selection count of the operators.

% problemName = {'UF1_','UF2','UF3','UF4','UF5','UF6','UF7'};
problemName = {'UF1_'};
selectors = {'Adaptive'};
selectorShort = {'AP'};
creditDef = { 'ParentDom','OffspringParetoFront','OffspringEArchive','ParetoFrontContribution','EArchiveContribution','OPa_BIR2PARENT','OPop_BIR2PARETOFRONT','OPop_BIR2ARCHIVE','CNI_BIR2PARETOFRONT','CNI_BIR2ARCHIVE'};
creditShort = {'ODP','OPopF','OPopEA','CPF','CEA','OPaR2','OPopPFR2','OPopEAR2','CPFR2','CEAR2'};

path = '/Users/nozomihitomi/Dropbox/MOHEA/';
res_path =strcat(path,'results');
origin = cd(res_path);
n_exps = length(problemName)*length(selectors)*length(creditDef);
exp_labels = cell(length(selectors)*length(creditDef),1);
nops= 6;
ops_labels = cell(nops,1);
data = zeros(n_exps,nops);

for i=1:length(problemName)
    exp_num = 1;
    for j=1:length(selectors)
        for k=1:length(creditDef)
            fileType =strcat(problemName{i},selectors{j},'*', creditDef{k},'*.hist');
            files = dir(fileType);
            tmpData = zeros(length(files),nops);
            for a=1:length(files)
                fid = fopen(files(a).name);
                op_num = 1;
                %read selected values from file. not necessarily in the
                %same order
                
            	tmpMap = java.util.HashMap;
                while(~feof(fid))
                    line = strsplit(fgetl(fid),',');
                    ops_labels{op_num} = line{1};
                    tmpMap.put(line{1},str2double(line{2}));
                    op_num = op_num +1;
                end
                sorted_labels = sort(ops_labels);
                for op_i = 1:length(sorted_labels)
                    tmpData(a,op_i) = tmpMap.get(sorted_labels{op_i});
                end
                %normalize by the number of total iterations so that we
                %get a percentage
                tmpData(a,:) = tmpData(a,:)/sum(tmpData(a,:));
            end
            data(exp_num,:)=mean(tmpData,1);
            exp_labels{exp_num} = strcat(selectorShort{j},'-',creditShort{k});
            exp_num = exp_num+1;
        end
    end
    bar(data,'stacked');
    legend(sorted_labels);
    set(gca,'XTickLabel',exp_labels)
end

cd(origin);
