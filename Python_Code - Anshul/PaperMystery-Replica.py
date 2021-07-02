from itertools import combinations
import json

######################################################################################################
######################################################################################################
#HEAVY WIP


def format_JSON(js_on): #ADD DOCSTRING
    data=[]
    [[data.extend([('''"'''+span["operationName"]+"_start"+'''"''',span["startTime"]),('''"'''+span["operationName"]+"_end"+'''"''',span["startTime"]+span["duration"])]) for span in trace["spans"]] for trace in json.loads(js_on)["data"]]
    return [data]

def extract_JSON(jsonFile):
    file=open(jsonFile)
    js_on=file.read()
    file.close()
    return js_on

def extract_JSONs(jsonList):
    data=[]
    [data.extend(format_JSON(extract_JSON(js_on))) for js_on in jsonList]
    return data

def threshold_eval(num,revnum,pT=("frequency",0)): #ADD DOCSTRING
    if pT[0]=="frequency":
        if revnum/(revnum+num)<=pT[1]:
            return True
    if revnum<=pT[1]:
        return True
    return False

def freq_eval(edge,edges,probThreshold=("frequency",0),dF=False): #add error for incorrect probThreshold type or incorrect input for threshold?, ADD DOCSTRING
    num=edges.count(edge)
    revnum=edges.count(edge[::-1])
    if threshold_eval(num,revnum,probThreshold):
        if dF=="odds":
            return [(edge,'''" '''+str(num)+":"+str(revnum)+'''"''')]
        elif dF=="frequency":
            return [(edge,'''" '''+str(round(num/(num+revnum),ndigits=3))+'''"''')]
        return [(edge,'''""''')]
    return []

def trace_rels(traceData,probThreshold=("frequency",0),displayFreq=False):
    """
    trace_rels(traceData,probThreshold=("frequency",0),displayFreq=False)

    PRIMARY USE: A "Main Function" used to infer temporal dependencies between edges in provided trace data.
                 This includes an optional frequency evaluation as described in the inputs below.
                 The output is compatible with the construct_DOT function if displayFrequency is provided.
    
    traceData --> All trace data that is to be processed.
                  Must be a tuple of all traces.
                  Each trace within this tuple must be a tuple of events, where each event is a tuple (eventName, timing).
    probThreshold --> A tuple with the threshold type and the threshold in form (<threshold type>, <threshold>).
                      Threshold type "frequency": Threshold is in range 0 <= threshold <= 1.
                                                  Edges are only considered "correct" if the frequency of violations of the edge (that is, edge(::-1))
                                                  among all cases where both elements of the edge appear together is less than or equal to the threshold.
                      Threshold type "discrete": Threshold is a nonnegative integer.
                                                Edges are only considered "correct" if the total amount of violations (that is, edge(::-1)) is less than
                                                or equal to the threshold.
    displayFreq --> A string indicating whether or not the "odds" should be displayed with "correct" edges, and in what manner.
                    displayFreq="frequency": (edge, frequency of aligned edges)
                    displayFreq="odds": (edge, aligned edges:violations)
                    Any other value for displayFreq is considered to mean that odds/frequency should not be displayed at all.        
    """
    edges=[]
    [[edges.append(edge) for edge in combinations(sorted(trace,key=lambda event:trace[event]),2) if not trace[edge[0]]==trace[edge[1]]] for trace in map(dict,traceData)] 
    ans=[]
    [ans.extend(freq_eval(edge,edges,probThreshold,displayFreq)) for edge in set(edges)]
    ans=dict(ans)
    return [(edge,ans[edge]) for edge in trans_reduct(ans)]

def trans_reduct(preceding):
    trans_closure = list(preceding)
    [[trans_closure.append((edge[0],pair[1])) for pair in preceding if pair[0]==edge[1] if (edge[0],pair[1]) not in trans_closure] for edge in trans_closure]
    filterList=[]
    [[filterList.append((edge[0],pair[1])) for pair in trans_closure if pair[0]==edge[1] if (edge[0],pair[1]) not in filterList] for edge in trans_closure]
    return [edge for edge in preceding if not edge in filterList]

def construct_DOT(preceding):
    return 'digraph '+'\n'.join(['{']+[' -> '.join(edge[0])+" [label= "+edge[1]+"]" for edge in preceding]+['}'])
